const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs-extra');
const chokidar = require('chokidar');
const chalk = require('chalk');
const ora = require('ora');

let javaProcess = null;
let isRestarting = false;
const isWindows = process.platform === 'win32';

async function devCommand(options) {
    console.log(chalk.cyan('\n🚀 ARTHA Development Server\n'));

    // Check if artha.json exists
    if (!fs.existsSync('artha.json')) {
        console.log(chalk.red('❌ No artha.json found!'));
        console.log(chalk.yellow('💡 Run: artha new <project-name> to create a project\n'));
        process.exit(1);
    }

    // Load config
    const config = await fs.readJson('artha.json');
    const port = options.port || config.server?.port || 8080;
    const srcDir = config.srcDir || 'src';

    // Find runtime JAR
    const runtimeJar = findRuntimeJar();
    if (!runtimeJar) {
        console.log(chalk.red('❌ ARTHA runtime not found!'));
        console.log(chalk.yellow('💡 Make sure you built the runtime: cd runtime && mvn package\n'));
        process.exit(1);
    }

    // Initial compile and start
    await compileAndStart(srcDir, runtimeJar, port);

    // Watch for changes
    console.log(chalk.gray('\n👀 Watching for changes... (Press Ctrl+C to stop)\n'));

    const watcher = chokidar.watch(`${srcDir}/**/*.java`, {
        ignored: /(^|[\/\\])\../,
        persistent: true
    });

    watcher.on('change', async (filePath) => {
        if (isRestarting) {
            console.log(chalk.gray('⏳ Already restarting, please wait...'));
            return;
        }

        isRestarting = true;
        console.log(chalk.yellow(`\n📝 File changed: ${filePath}`));

        if (javaProcess) {
            await killServerGracefully();
        }

        await compileAndStart(srcDir, runtimeJar, port);
        isRestarting = false;
    });

    // Handle Ctrl+C
    process.on('SIGINT', async () => {
        console.log(chalk.yellow('\n\n👋 Stopping ARTHA server...'));
        if (javaProcess) {
            await killServerGracefully();
        }
        watcher.close();
        process.exit(0);
    });
}

async function killServerGracefully() {
    return new Promise((resolve) => {
        if (!javaProcess) {
            resolve();
            return;
        }

        javaProcess.on('exit', () => {
            javaProcess = null;
            resolve();
        });

        // Kill the process
        if (isWindows) {
            spawn('taskkill', ['/pid', javaProcess.pid, '/f', '/t']);
        } else {
            javaProcess.kill('SIGTERM');
        }

        // Force kill after 3 seconds if still running
        setTimeout(() => {
            if (javaProcess) {
                javaProcess.kill('SIGKILL');
                javaProcess = null;
            }
            resolve();
        }, 3000);
    });
}

async function compileAndStart(srcDir, runtimeJar, port) {
    const spinner = ora('Compiling...').start();

    try {
        // Create build directory
        await fs.ensureDir('build');

        // Find all Java files
        const javaFiles = await findJavaFiles(srcDir);

        if (javaFiles.length === 0) {
            spinner.fail(chalk.red('No Java files found in ' + srcDir));
            return;
        }

        // Compile
        await compile(javaFiles, runtimeJar);
        spinner.succeed(chalk.green('Compiled successfully'));

        // Small delay to ensure port is free
        await new Promise(resolve => setTimeout(resolve, 500));

        // Start server
        startServer(runtimeJar, port);

    } catch (error) {
        spinner.fail(chalk.red('Compilation failed'));
        console.log(chalk.red('\n' + error.message));
    }
}

function compile(javaFiles, runtimeJar) {
    return new Promise((resolve, reject) => {
        const filesArg = javaFiles.join(' ');
        const command = isWindows
            ? `javac -cp "${runtimeJar}" -d build ${filesArg}`
            : `javac -cp "${runtimeJar}" -d build ${filesArg}`;

        const javac = spawn(command, {
            shell: true,
            stdio: ['pipe', 'pipe', 'pipe']
        });

        let stderr = '';

        javac.stderr.on('data', (data) => {
            stderr += data.toString();
        });

        javac.on('close', (code) => {
            if (code !== 0) {
                reject(new Error(stderr));
            } else {
                resolve();
            }
        });
    });
}

function startServer(runtimeJar, port) {
    const classpath = isWindows
        ? `${runtimeJar};build`
        : `${runtimeJar}:build`;

    javaProcess = spawn('java', [
        `-Dartha.port=${port}`,
        '-cp',
        classpath,
        'dev.artha.core.Runtime'
    ], {
        stdio: 'inherit'
    });

    javaProcess.on('error', (error) => {
        console.log(chalk.red('❌ Failed to start server:'), error.message);
    });

    javaProcess.on('exit', (code) => {
        if (code !== 0 && code !== null && !isRestarting) {
            console.log(chalk.red(`\n❌ Server stopped with code ${code}`));
        }
    });
}

function findRuntimeJar() {
    // Try multiple possible locations
    const possiblePaths = [
        // From examples/01-hello-world
        path.join(process.cwd(), '..', '..', 'runtime', 'target'),
        // From examples
        path.join(process.cwd(), '..', 'runtime', 'target'),
        // From root
        path.join(process.cwd(), 'runtime', 'target'),
        // Relative to CLI location
        path.join(__dirname, '..', '..', '..', 'runtime', 'target'),
        // Absolute path (adjust if needed)
        'C:\\Coding\\Java\\Spring\\artha\\runtime\\target'
    ];

    for (const runtimePath of possiblePaths) {
        if (fs.existsSync(runtimePath)) {
            const files = fs.readdirSync(runtimePath);
            const jarFile = files.find(f =>
                f.startsWith('artha-runtime') &&
                f.endsWith('.jar') &&
                !f.endsWith('-sources.jar') &&
                !f.endsWith('-javadoc.jar')
            );

            if (jarFile) {
                const fullPath = path.join(runtimePath, jarFile);
                console.log(chalk.green(`✅ Found runtime: ${jarFile}`));
                return fullPath;
            }
        }
    }

    return null;
}

async function findJavaFiles(dir) {
    const files = [];

    async function scan(directory) {
        const items = await fs.readdir(directory);

        for (const item of items) {
            const fullPath = path.join(directory, item);
            const stat = await fs.stat(fullPath);

            if (stat.isDirectory()) {
                await scan(fullPath);
            } else if (item.endsWith('.java')) {
                files.push(fullPath);
            }
        }
    }

    await scan(dir);
    return files;
}

module.exports = devCommand;

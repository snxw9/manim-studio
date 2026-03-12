const { spawn, execSync } = require('child_process');
const path = require('path');
const net = require('net');
const fs = require('fs');

const ENGINE_DIR = path.resolve(__dirname, '../../engine');
const PORT = 8000;
const isWin = process.platform === 'win32';

const VENV_PYTHON = isWin
  ? path.join(ENGINE_DIR, 'venv', 'Scripts', 'python.exe')
  : path.join(ENGINE_DIR, 'venv', 'bin', 'python');

const VENV_PIP = isWin
  ? path.join(ENGINE_DIR, 'venv', 'Scripts', 'pip.exe')
  : path.join(ENGINE_DIR, 'venv', 'bin', 'pip');

function log(msg) {
  process.stdout.write(`\x1b[33m[ENGINE]\x1b[0m ${msg}\n`);
}

function isPortInUse(port) {
  return new Promise((resolve) => {
    const client = net.connect({ port, host: '127.0.0.1' }, () => {
      client.destroy();
      resolve(true);
    });
    client.on('error', () => resolve(false));
  });
}

function run(cmd, opts = {}) {
  try {
    execSync(cmd, { stdio: 'inherit', ...opts });
    return true;
  } catch (e) {
    log(`Command failed: ${cmd}`);
    log(e.message);
    return false;
  }
}

async function main() {
  if (await isPortInUse(PORT)) {
    log('Already running on port 8000 ✓');
    await new Promise(() => {});
  }

  if (!fs.existsSync(ENGINE_DIR)) {
    log(`ERROR: engine directory not found at ${ENGINE_DIR}`);
    log('Make sure the engine/ folder exists at the project root');
    process.exit(1);
  }

  if (!fs.existsSync(VENV_PYTHON)) {
    log('Creating Python virtual environment...');
    const ok = run(`python -m venv "${path.join(ENGINE_DIR, 'venv')}"`, { cwd: ENGINE_DIR });
    if (!ok) {
      log('ERROR: Could not create venv. Is Python installed and on your PATH?');
      log('Install Python from https://python.org then re-run npm run dev');
      process.exit(1);
    }
  }

  const reqFile = path.join(ENGINE_DIR, 'requirements.txt');
  if (fs.existsSync(reqFile)) {
    log('Installing/checking Python dependencies...');
    run(`"${VENV_PIP}" install -r "${reqFile}" -q`);
  }

  log(`Starting engine from ${ENGINE_DIR}`);
  log(`Using Python: ${VENV_PYTHON}`);

  const proc = spawn(
    VENV_PYTHON,
    ['-m', 'uvicorn', 'main:app', '--reload', '--port', '8000', '--host', '127.0.0.1'],
    {
      cwd: ENGINE_DIR,
      stdio: ['ignore', 'pipe', 'pipe'],
      windowsHide: true,
    }
  );

  proc.stdout.on('data', (d) => {
    d.toString().trim().split('\n').forEach((line) => {
      if (line.trim()) log(line);
    });
  });

  proc.stderr.on('data', (d) => {
    d.toString().trim().split('\n').forEach((line) => {
      if (line.trim()) log(line);
    });
  });

  proc.on('error', (err) => {
    log(`Failed to start engine: ${err.message}`);
    if (err.code === 'ENOENT') {
      log(`Python not found at: ${VENV_PYTHON}`);
      log('Try deleting engine/venv and re-running npm run dev');
    }
  });

  proc.on('exit', (code) => {
    if (code !== 0 && code !== null) {
      log(`Engine exited with code ${code}`);
    }
  });

  const cleanup = () => {
    try { proc.kill(); } catch {}
    process.exit(0);
  };
  process.on('exit', () => { try { proc.kill(); } catch {} });
  process.on('SIGINT', cleanup);
  process.on('SIGTERM', cleanup);

  await new Promise(() => {});
}

main().catch((err) => {
  log(`Fatal error: ${err.message}`);
  process.exit(1);
});

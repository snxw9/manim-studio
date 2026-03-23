// web/scripts/engine.js
// Starts the Python engine before Next.js.
// Keeps it alive. Logs clearly. No complex logic.

const { spawn } = require('child_process');
const path = require('path');
const net = require('net');
const fs = require('fs');
const http = require('http');

const ENGINE_DIR = path.resolve(__dirname, '../../engine');
const PORT = 8000;
const IS_WIN = process.platform === 'win32';

const PYTHON = IS_WIN
  ? path.join(ENGINE_DIR, 'venv', 'Scripts', 'python.exe')
  : path.join(ENGINE_DIR, 'venv', 'bin', 'python');

function log(msg) {
  process.stdout.write(`\x1b[33m[ENGINE]\x1b[0m ${msg}\n`);
}

function portInUse(port) {
  return new Promise(resolve => {
    const s = net.createServer();
    s.once('error', () => resolve(true));
    s.once('listening', () => { s.close(); resolve(false); });
    s.listen(port, '127.0.0.1');
  });
}

function waitForHealth(ms = 30000) {
  return new Promise((resolve, reject) => {
    const deadline = Date.now() + ms;
    const check = () => {
      http.get('http://127.0.0.1:8000/health', res => {
        if (res.statusCode === 200) return resolve();
        retry();
      }).on('error', retry);
    };
    const retry = () => {
      if (Date.now() > deadline) return reject(new Error('Engine did not respond in time'));
      setTimeout(check, 1000);
    };
    check();
  });
}

async function main() {
  // Already running?
  if (await portInUse(PORT)) {
    log('Port 8000 already in use — assuming engine is running');
    await new Promise(() => {});
    return;
  }

  // Python venv exists?
  if (!fs.existsSync(PYTHON)) {
    log('ERROR: Python venv not found');
    log(`Expected: ${PYTHON}`);
    log('Fix: cd engine && python -m venv venv && venv\\Scripts\\pip install -r requirements.txt');
    log('Engine will not start. Web app starting without engine.');
    await new Promise(() => {});
    return;
  }

  // Engine dir exists?
  if (!fs.existsSync(ENGINE_DIR)) {
    log(`ERROR: engine/ directory not found at ${ENGINE_DIR}`);
    await new Promise(() => {});
    return;
  }

  log('Starting engine...');

  const proc = spawn(PYTHON, [
    '-m', 'uvicorn', 'main:app',
    '--host', '127.0.0.1',
    '--port', String(PORT),
    '--reload',
  ], {
    cwd: ENGINE_DIR,
    windowsHide: true,
    stdio: ['ignore', 'pipe', 'pipe'],
  });

  proc.stdout.on('data', d =>
    d.toString().trim().split('\n')
      .filter(l => l.trim())
      .forEach(l => log(l))
  );

  proc.stderr.on('data', d =>
    d.toString().trim().split('\n')
      .filter(l => l.trim())
      .forEach(l => log(l))
  );

  proc.on('error', err => {
    log(`Failed to spawn engine: ${err.message}`);
    if (err.code === 'ENOENT') {
      log(`Python not found: ${PYTHON}`);
      log('Delete engine/venv and run npm run dev again to rebuild it');
    }
  });

  proc.on('exit', (code, signal) => {
    if (code !== 0 && code !== null) {
      log(`Engine process exited (code ${code})`);
      log('Check the error above. Common causes:');
      log('  - Syntax error in main.py or a Python file');
      log('  - Missing dependency: cd engine && venv\\Scripts\\pip install -r requirements.txt');
      log('  - Port 8000 already in use: taskkill /F /IM python.exe');
    }
  });

  // Wait for engine to be healthy
  try {
    await waitForHealth(45000);
    log('Engine ready on http://127.0.0.1:8000');
  } catch {
    log('Engine did not respond to health check in 45s');
    log('Check the error messages above');
    log('Web app starting anyway — some features will not work');
  }

  // Keep script alive
  const kill = () => { try { proc.kill(); } catch {} process.exit(0); };
  process.on('SIGINT', kill);
  process.on('SIGTERM', kill);
  process.on('exit', () => { try { proc.kill(); } catch {} });

  await new Promise(() => {});
}

main().catch(err => {
  log(`Fatal: ${err.message}`);
  process.exit(1);
});

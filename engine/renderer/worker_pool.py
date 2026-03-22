"""
Pool of persistent Manim worker processes.
Keeps NUM_WORKERS warm processes alive to handle render jobs.
"""
import asyncio
import json
import sys
from pathlib import Path

NUM_WORKERS = 2  # Two workers = two parallel renders
WORKER_SCRIPT = str(Path(__file__).parent / "worker.py")
VENV_PYTHON = sys.executable


class RenderWorker:
    def __init__(self):
        self.process = None
        self.lock = asyncio.Lock()

    async def start(self):
        self.process = await asyncio.create_subprocess_exec(
            VENV_PYTHON, WORKER_SCRIPT,
            stdin=asyncio.subprocess.PIPE,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE,
        )
        print(f"[worker] Started PID {self.process.pid}")

    async def render(self, job: dict) -> dict:
        async with self.lock:
            if self.process is None or self.process.returncode is not None:
                print("[worker] Restarting dead worker...")
                await self.start()

            job_line = (json.dumps(job) + "\n").encode()
            self.process.stdin.write(job_line)
            await self.process.stdin.drain()

            try:
                result_line = await asyncio.wait_for(
                    self.process.stdout.readline(),
                    timeout=300,
                )
                if not result_line:
                    raise RuntimeError("Worker process died unexpectedly")
                return json.loads(result_line.decode().strip())
            except asyncio.TimeoutError:
                if self.process:
                    self.process.kill()
                self.process = None
                return {"error": "Render timed out"}
            except Exception as e:
                if self.process:
                    self.process.kill()
                self.process = None
                return {"error": str(e)}

    async def stop(self):
        if self.process:
            try:
                self.process.kill()
                await self.process.wait()
            except:
                pass
            self.process = None


class WorkerPool:
    def __init__(self, size: int = NUM_WORKERS):
        self.workers = [RenderWorker() for _ in range(size)]
        self._idx = 0

    async def start(self):
        for w in self.workers:
            await w.start()
        print(f"[pool] {len(self.workers)} render workers ready")

    async def render(self, job: dict) -> dict:
        # Round-robin across workers
        worker = self.workers[self._idx % len(self.workers)]
        self._idx += 1
        return await worker.render(job)

    async def stop(self):
        for w in self.workers:
            await w.stop()


# Global pool
_pool: WorkerPool | None = None

def get_worker_pool() -> WorkerPool:
    global _pool
    if _pool is None:
        _pool = WorkerPool()
    return _pool

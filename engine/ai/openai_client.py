from openai import AsyncOpenAI
import os

client = AsyncOpenAI(api_key=os.environ.get("OPENAI_API_KEY", "dummy_key"))

async def generate(prompt: str, model: str = "gpt-4o-mini") -> str:
    response = await client.chat.completions.create(
        model=model,
        messages=[{"role": "user", "content": prompt}],
        temperature=0.2,
        max_tokens=4096,
    )
    return response.choices[0].message.content.strip()

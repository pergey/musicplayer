import redis.asyncio as redis
from config import REDIS_URL


async def get_redis():
    redis_client = redis.from_url(REDIS_URL, decode_responses=True)
    yield redis_client
    await redis_client.aclose()

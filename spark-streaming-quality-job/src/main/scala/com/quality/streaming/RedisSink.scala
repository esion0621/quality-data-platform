package com.quality.streaming

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

object RedisSink {
  private val redisHost = "master"      // 根据实际修改
  private val redisPort = 6379
  private val redisTimeout = 2000

  @transient private lazy val jedisPool: JedisPool = {
    val config = new JedisPoolConfig
    config.setMaxTotal(10)
    config.setMaxIdle(5)
    config.setMinIdle(1)
    config.setTestOnBorrow(true)
    config.setTestOnReturn(true)
    config.setTestWhileIdle(true)
    new JedisPool(config, redisHost, redisPort, redisTimeout)
  }

  def getJedis: Jedis = jedisPool.getResource

  def returnJedis(jedis: Jedis): Unit = if (jedis != null) jedis.close()
}

package com.nidoham.social.helpers

import com.nidoham.social.domain.profile.Profile

object ProfileCache {

    private val cache = mutableMapOf<String, Profile>()
    private val timestamps = mutableMapOf<String, Long>()
    private const val CACHE_DURATION = 5 * 60 * 1000L

    fun put(id: String, profile: Profile) {
        cache[id] = profile
        timestamps[id] = System.currentTimeMillis()
    }

    fun get(id: String): Profile? {
        val timestamp = timestamps[id] ?: return null
        val now = System.currentTimeMillis()

        if (now - timestamp > CACHE_DURATION) {
            remove(id)
            return null
        }

        return cache[id]
    }

    fun remove(id: String) {
        cache.remove(id)
        timestamps.remove(id)
    }

    fun clear() {
        cache.clear()
        timestamps.clear()
    }

    fun putAll(profiles: List<Profile>) {
        profiles.forEach { put(it.username, it) }
    }

    fun getAll(ids: List<String>): List<Profile> {
        return ids.mapNotNull { get(it) }
    }

    fun has(id: String): Boolean = get(id) != null

    fun size(): Int = cache.size

    fun cleanup() {
        val now = System.currentTimeMillis()
        val expired = timestamps.filter { (_, time) ->
            now - time > CACHE_DURATION
        }.keys
        expired.forEach { remove(it) }
    }
}
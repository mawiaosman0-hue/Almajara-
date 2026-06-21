package com.example.data.network

import android.content.Context

object SupabaseConfig {
    var url: String = "https://figyszyedxlmbtaepmyt.supabase.co/"
    var apiKey: String = "sb_publishable_WRJgX0HreyiRExm-d5OSVQ_sZwnWYBy"
    
    private fun sanitizeUrl(input: String): String {
        var clean = input.trim()
        if (clean.isEmpty()) return "https://figyszyedxlmbtaepmyt.supabase.co/"
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        if (clean.contains("/rest/v1")) {
            clean = clean.substringBefore("/rest/v1")
        }
        if (!clean.endsWith("/")) {
            clean += "/"
        }
        return clean
    }

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)
        val rawUrl = prefs.getString("supabase_url", "https://figyszyedxlmbtaepmyt.supabase.co/") ?: "https://figyszyedxlmbtaepmyt.supabase.co/"
        url = sanitizeUrl(rawUrl)
        
        var savedKey = prefs.getString("supabase_api_key", "sb_publishable_WRJgX0HreyiRExm-d5OSVQ_sZwnWYBy") ?: "sb_publishable_WRJgX0HreyiRExm-d5OSVQ_sZwnWYBy"
        if (savedKey.startsWith("Sb_")) {
            savedKey = "sb_" + savedKey.substring(3)
            prefs.edit().putString("supabase_api_key", savedKey).apply()
        }
        apiKey = savedKey
    }

    fun save(context: Context, newUrl: String, newKey: String) {
        val cleanUrl = sanitizeUrl(newUrl)
        var cleanKey = newKey.trim()
        if (cleanKey.startsWith("Sb_")) {
            cleanKey = "sb_" + cleanKey.substring(3)
        }
        val prefs = context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("supabase_url", cleanUrl)
            .putString("supabase_api_key", cleanKey)
            .apply()
        url = cleanUrl
        apiKey = cleanKey
    }
}

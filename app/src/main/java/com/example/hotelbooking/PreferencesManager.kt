import android.content.Context

object PreferencesManager {
    private const val PREFS_NAME = "preferences"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    // This will be called to check if onboarding has been completed
    fun isOnboardingCompleted(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    // This will be called to set onboarding as completed
    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    // This will be called to initialize preferences during first app launch
    fun initializeOnboardingFlag(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // If the flag is not set, it means this is the first time launching the app
        if (!sharedPreferences.contains(KEY_ONBOARDING_COMPLETED)) {
            setOnboardingCompleted(context, false)  // Set onboarding as not completed initially
        }
    }
}

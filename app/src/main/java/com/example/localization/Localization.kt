package com.example.localization

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    EN("en", "English", "English"),
    HI("hi", "Hindi", "हिंदी"),
    PA("pa", "Punjabi", "ਪੰਜਾਬੀ")
}

object Localization {

    fun getGreeting(language: AppLanguage, timeOfDay: TimeOfDay, userName: String): String {
        val firstName = userName.trim().split("\\s+".toRegex()).firstOrNull()?.takeIf { it.isNotBlank() } ?: "Farmer"
        return when (language) {
            AppLanguage.EN -> when (timeOfDay) {
                TimeOfDay.MORNING -> "Good morning, $firstName!"
                TimeOfDay.AFTERNOON -> "Good afternoon, $firstName!"
                TimeOfDay.EVENING -> "Good evening, $firstName!"
            }
            AppLanguage.HI -> when (timeOfDay) {
                TimeOfDay.MORNING -> "सुप्रभात, $firstName!"
                TimeOfDay.AFTERNOON -> "शुभ दोपहर, $firstName!"
                TimeOfDay.EVENING -> "शुभ संध्या, $firstName!"
            }
            AppLanguage.PA -> when (timeOfDay) {
                TimeOfDay.MORNING -> "ਸਤ ਸ੍ਰੀ ਅਕਾਲ, $firstName!"
                TimeOfDay.AFTERNOON -> "ਸ਼ੁਭ ਦੁਪਹਿਰ, $firstName!"
                TimeOfDay.EVENING -> "ਸ਼ੁਭ ਸ਼ਾਮ, $firstName!"
            }
        }
    }

    private val translations: Map<String, Map<AppLanguage, String>> = mapOf(
        // App & Navigation
        "app_name" to mapOf(AppLanguage.EN to "AgriX AI", AppLanguage.HI to "AgriX AI", AppLanguage.PA to "AgriX AI"),
        "nav_home" to mapOf(AppLanguage.EN to "Home", AppLanguage.HI to "होम", AppLanguage.PA to "ਹੋਮ"),
        "nav_farm" to mapOf(AppLanguage.EN to "Farm", AppLanguage.HI to "फार्म", AppLanguage.PA to "ਫਾਰਮ"),
        "nav_ai_assistant" to mapOf(AppLanguage.EN to "AI Assistant", AppLanguage.HI to "AI सहायक", AppLanguage.PA to "AI ਸਹਾਇਕ"),
        "nav_guides" to mapOf(AppLanguage.EN to "Guides", AppLanguage.HI to "गाइड्स", AppLanguage.PA to "ਗਾਈਡਾਂ"),
        "nav_profile" to mapOf(AppLanguage.EN to "Profile", AppLanguage.HI to "प्रोफ़ाइल", AppLanguage.PA to "ਪ੍ਰੋਫਾਈਲ"),

        // Home Screen
        "my_fields" to mapOf(AppLanguage.EN to "My Fields", AppLanguage.HI to "मेरे खेत", AppLanguage.PA to "ਮੇਰੇ ਖੇਤ"),
        "view_all" to mapOf(AppLanguage.EN to "View All", AppLanguage.HI to "सभी देखें", AppLanguage.PA to "ਸਾਰੇ ਦੇਖੋ"),
        "todays_forecast" to mapOf(AppLanguage.EN to "Today's Farming Forecast", AppLanguage.HI to "आज का कृषि पूर्वानुमान", AppLanguage.PA to "ਅੱਜ ਦਾ ਖੇਤੀ ਪੂਰਵ-ਅਨੁਮਾਨ"),
        "humidity" to mapOf(AppLanguage.EN to "Humidity", AppLanguage.HI to "नमी", AppLanguage.PA to "ਨਮੀ"),
        "wind" to mapOf(AppLanguage.EN to "Wind", AppLanguage.HI to "हवा", AppLanguage.PA to "ਹਵਾ"),
        "rain_chance" to mapOf(AppLanguage.EN to "Rain Chance", AppLanguage.HI to "बारिश की संभावना", AppLanguage.PA to "ਮੀਂਹ ਦੀ ਸੰਭਾਵਨਾ"),
        "crops" to mapOf(AppLanguage.EN to "Crops", AppLanguage.HI to "फ़सलें", AppLanguage.PA to "ਫ਼ਸਲਾਂ"),
        "acres" to mapOf(AppLanguage.EN to "Acres", AppLanguage.HI to "एकड़", AppLanguage.PA to "ਏਕੜ"),
        "soil_health" to mapOf(AppLanguage.EN to "Soil Health", AppLanguage.HI to "मृदा स्वास्थ्य", AppLanguage.PA to "ਮਿੱਟੀ ਦੀ ਸਿਹਤ"),

        // Quick Actions
        "action_crop_rec" to mapOf(AppLanguage.EN to "Crop Recommendation", AppLanguage.HI to "फसल सिफारिश", AppLanguage.PA to "ਫ਼ਸਲ ਦੀ ਸਿਫ਼ਾਰਸ਼"),
        "action_fertilizer_guide" to mapOf(AppLanguage.EN to "Fertilizer & Seed Guide", AppLanguage.HI to "उर्वरक और बीज गाइड", AppLanguage.PA to "ਖਾਦ ਅਤੇ ਬੀਜ ਗਾਈਡ"),
        "action_weather_update" to mapOf(AppLanguage.EN to "Weather Update", AppLanguage.HI to "मौसम अपडेट", AppLanguage.PA to "ਮੌਸਮ ਅੱਪਡੇਟ"),
        "action_farming_tips" to mapOf(AppLanguage.EN to "Farming Tips", AppLanguage.HI to "खेती के सुझाव", AppLanguage.PA to "ਖੇਤੀ ਦੇ ਸੁਝਾਅ"),
        "action_disease_detection" to mapOf(AppLanguage.EN to "Disease Detection", AppLanguage.HI to "रोग पहचान", AppLanguage.PA to "ਬਿਮਾਰੀ ਦੀ ਪਛਾਣ"),

        // Weather
        "weather_title" to mapOf(AppLanguage.EN to "Weather Details", AppLanguage.HI to "मौसम विवरण", AppLanguage.PA to "ਮੌਸਮ ਦੇ ਵੇਰਵੇ"),
        "current_weather" to mapOf(AppLanguage.EN to "Current Weather", AppLanguage.HI to "वर्तमान मौसम", AppLanguage.PA to "ਮੌਜੂਦਾ ਮੌਸਮ"),
        "weather_forecast_5days" to mapOf(AppLanguage.EN to "5-Day Farming Forecast", AppLanguage.HI to "5-दिवसीय कृषि पूर्वानुमान", AppLanguage.PA to "5-ਦਿਨਾਂ ਦਾ ਖੇਤੀ ਪੂਰਵ-ਅਨੁਮਾਨ"),
        "change_location" to mapOf(AppLanguage.EN to "Change Location", AppLanguage.HI to "स्थान बदलें", AppLanguage.PA to "ਸਥਾਨ ਬਦਲੋ"),
        "refresh" to mapOf(AppLanguage.EN to "Refresh", AppLanguage.HI to "ताज़ा करें", AppLanguage.PA to "ਤਾਜ਼ਾ ਕਰੋ"),
        "weather_error" to mapOf(AppLanguage.EN to "Unable to load weather data", AppLanguage.HI to "मौसम डेटा लोड करने में असमर्थ", AppLanguage.PA to "ਮੌਸਮ ਡਾਟਾ ਲੋਡ ਕਰਨ ਵਿੱਚ ਅਸਮਰੱਥ"),
        "gps_verified" to mapOf(AppLanguage.EN to "Live GPS Location", AppLanguage.HI to "लाइव GPS स्थान", AppLanguage.PA to "ਲਾਈਵ GPS ਸਥਾਨ"),
        "manual_location" to mapOf(AppLanguage.EN to "Manual / Profile Location", AppLanguage.HI to "मैनुअल / प्रोफ़ाइल स्थान", AppLanguage.PA to "ਮੈਨੂਅਲ / ਪ੍ਰੋਫਾਈਲ ਸਥਾਨ"),

        // Crop Recommendation
        "crop_rec_title" to mapOf(AppLanguage.EN to "Crop Recommendation", AppLanguage.HI to "फसल सिफारिश", AppLanguage.PA to "ਫ਼ਸਲ ਦੀ ਸਿਫ਼ਾਰਸ਼"),
        "soil_type" to mapOf(AppLanguage.EN to "Soil Type", AppLanguage.HI to "मिट्टी का प्रकार", AppLanguage.PA to "ਮਿੱਟੀ ਦੀ ਕਿਸਮ"),
        "season" to mapOf(AppLanguage.EN to "Season", AppLanguage.HI to "मौसम / सीज़न", AppLanguage.PA to "ਸੀਜ਼ਨ"),
        "water_availability" to mapOf(AppLanguage.EN to "Water Availability", AppLanguage.HI to "जल उपलब्धता", AppLanguage.PA to "ਪਾਣੀ ਦੀ ਉਪਲਬਧਤਾ"),
        "land_size" to mapOf(AppLanguage.EN to "Land Size (Acres)", AppLanguage.HI to "भूमि का आकार (एकड़)", AppLanguage.PA to "ਜ਼ਮੀਨ ਦਾ ਆਕਾਰ (ਏਕੜ)"),
        "get_recommendations" to mapOf(AppLanguage.EN to "Get Recommendations", AppLanguage.HI to "सिफारिशें प्राप्त करें", AppLanguage.PA to "ਸਿਫ਼ਾਰਸ਼ਾਂ ਪ੍ਰਾਪਤ ਕਰੋ"),
        "recommended_crops" to mapOf(AppLanguage.EN to "Recommended Crops", AppLanguage.HI to "अनुशंसित फसलें", AppLanguage.PA to "ਸਿਫਾਰਸ਼ ਕੀਤੀਆਂ ਫਸਲਾਂ"),
        "high_suitability" to mapOf(AppLanguage.EN to "High Suitability", AppLanguage.HI to "उच्च उपयुक्तता", AppLanguage.PA to "ਉੱਚ ਢੁਕਵਾਂ"),
        "moderate_suitability" to mapOf(AppLanguage.EN to "Moderate Suitability", AppLanguage.HI to "मध्यम उपयुक्तता", AppLanguage.PA to "ਦਰਮਿਆਨਾ ਢੁਕਵਾਂ"),
        "low_suitability" to mapOf(AppLanguage.EN to "Low Suitability", AppLanguage.HI to "कम उपयुक्तता", AppLanguage.PA to "ਘੱਟ ਢੁਕਵਾਂ"),
        "source_ai" to mapOf(AppLanguage.EN to "AI-Generated Recommendation", AppLanguage.HI to "AI द्वारा जनित सिफारिश", AppLanguage.PA to "AI ਦੁਆਰਾ ਤਿਆਰ ਸਿਫ਼ਾਰਸ਼"),
        "source_local_fallback" to mapOf(AppLanguage.EN to "Local Rule-Based Agronomic Estimate", AppLanguage.HI to "स्थानीय नियम-आधारित कृषि अनुमान", AppLanguage.PA to "ਸਥਾਨਕ ਨਿਯਮ-ਅਧਾਰਿਤ ਖੇਤੀ ਅੰਦਾਜ਼ਾ"),
        "crop_disclaimer" to mapOf(
            AppLanguage.EN to "Agronomic Disclaimer: Crop recommendations are general guidance. Validate against local soil health card testing and Krishi Vigyan Kendra (KVK) extension advice before sowing.",
            AppLanguage.HI to "कृषि अस्वीकरण: फसल सिफारिशें सामान्य मार्गदर्शन हैं। बुआई से पहले स्थानीय मृदा स्वास्थ्य कार्ड परीक्षण और कृषि विज्ञान केंद्र (KVK) की सलाह से पुष्टि करें।",
            AppLanguage.PA to "ਖੇਤੀਬਾੜੀ ਬੇਦਾਅਵਾ: ਫ਼ਸਲ ਦੀਆਂ ਸਿਫ਼ਾਰਸ਼ਾਂ ਆਮ ਸੇਧ ਹਨ। ਬੀਜਣ ਤੋਂ ਪਹਿਲਾਂ ਮਿੱਟੀ ਦੀ ਜਾਂਚ ਅਤੇ KVK ਮਾਹਿਰਾਂ ਦੀ ਸਲਾਹ ਲਓ।"
        ),
        "error_invalid_land_size" to mapOf(
            AppLanguage.EN to "Please enter a valid land size between 0.1 and 100,000 acres.",
            AppLanguage.HI to "कृपया 0.1 से 100,000 एकड़ के बीच मान्य भूमि आकार दर्ज करें।",
            AppLanguage.PA to "ਕਿਰਪਾ ਕਰਕੇ 0.1 ਤੋਂ 100,000 ਏਕੜ ਦੇ ਵਿਚਕਾਰ ਸਹੀ ਜ਼ਮੀਨ ਦਾ ਆਕਾਰ ਦਰਜ ਕਰੋ।"
        ),

        // Disease Detection
        "disease_detection_title" to mapOf(AppLanguage.EN to "Plant Disease Detection", AppLanguage.HI to "पौधों की बीमारी की पहचान", AppLanguage.PA to "ਪੌਦਿਆਂ ਦੀ ਬਿਮਾਰੀ ਦੀ ਪਛਾਣ"),
        "upload_leaf_image" to mapOf(AppLanguage.EN to "Upload Crop / Leaf Photo", AppLanguage.HI to "फसल / पत्ते की फोटो अपलोड करें", AppLanguage.PA to "ਫ਼ਸਲ / ਪੱਤੇ ਦੀ ਫੋਟੋ ਅੱਪਲੋਡ ਕਰੋ"),
        "take_photo" to mapOf(AppLanguage.EN to "Take Photo", AppLanguage.HI to "फोटो खींचें", AppLanguage.PA to "ਫੋਟੋ ਖਿੱਚੋ"),
        "choose_gallery" to mapOf(AppLanguage.EN to "Choose from Gallery", AppLanguage.HI to "गैलरी से चुनें", AppLanguage.PA to "ਗੈਲਰੀ ਤੋਂ ਚੁਣੋ"),
        "analyze_crop" to mapOf(AppLanguage.EN to "Analyze Plant Health", AppLanguage.HI to "पौधे के स्वास्थ्य का विश्लेषण करें", AppLanguage.PA to "ਪੌਦੇ ਦੀ ਸਿਹਤ ਦਾ ਵਿਸ਼ਲੇਸ਼ਣ ਕਰੋ"),
        "analyzing_image" to mapOf(AppLanguage.EN to "Analyzing plant image with AI...", AppLanguage.HI to "AI के साथ पौधे की छवि का विश्लेषण किया जा रहा है...", AppLanguage.PA to "AI ਨਾਲ ਪੌਦੇ ਦੀ ਤਸਵੀਰ ਦਾ ਵਿਸ਼ਲੇਸ਼ਣ ਕੀਤਾ ਜਾ ਰਿਹਾ ਹੈ..."),
        "diagnosis_result" to mapOf(AppLanguage.EN to "Diagnosis Result", AppLanguage.HI to "निदान परिणाम", AppLanguage.PA to "ਨਿਦਾਨ ਦੇ ਨਤੀਜੇ"),
        "symptoms" to mapOf(AppLanguage.EN to "Symptoms", AppLanguage.HI to "लक्षण", AppLanguage.PA to "ਲੱਛਣ"),
        "treatment" to mapOf(AppLanguage.EN to "Treatment & Cure", AppLanguage.HI to "उपचार और रोकथाम", AppLanguage.PA to "ਇਲਾਜ ਅਤੇ ਰੋਕਥਾਮ"),
        "prevention" to mapOf(AppLanguage.EN to "Prevention Tips", AppLanguage.HI to "बचाव के उपाय", AppLanguage.PA to "ਬਚਾਅ ਦੇ ਸੁਝਾਅ"),
        "confidence" to mapOf(AppLanguage.EN to "AI Confidence", AppLanguage.HI to "AI आत्मविश्वास", AppLanguage.PA to "AI ਵਿਸ਼ਵਾਸ"),
        "disease_error" to mapOf(AppLanguage.EN to "Unable to analyze this image. Please try again.", AppLanguage.HI to "इस छवि का विश्लेषण करने में असमर्थ। कृपया पुनः प्रयास करें।", AppLanguage.PA to "ਇਸ ਤਸਵੀਰ ਦਾ ਵਿਸ਼ਲੇਸ਼ਣ ਕਰਨ ਵਿੱਚ ਅਸਮਰੱਥ। ਕਿਰਪਾ ਕਰਕੇ ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ।"),

        // AI Farming Assistant
        "ai_assistant_title" to mapOf(AppLanguage.EN to "AI Farming Assistant", AppLanguage.HI to "AI कृषि सहायक", AppLanguage.PA to "AI ਖੇਤੀਬਾੜੀ ਸਹਾਇਕ"),
        "ai_subtitle" to mapOf(AppLanguage.EN to "Ask anything about crops, pests, fertilizers or weather", AppLanguage.HI to "फसलों, कीड़ों, उर्वरकों या मौसम के बारे में कुछ भी पूछें", AppLanguage.PA to "ਫ਼ਸਲਾਂ, ਕੀੜਿਆਂ, ਖਾਦਾਂ ਜਾਂ ਮੌਸਮ ਬਾਰੇ ਕੁਝ ਵੀ ਪੁੱਛੋ"),
        "ask_placeholder" to mapOf(AppLanguage.EN to "Type your farming question...", AppLanguage.HI to "अपना खेती का सवाल लिखें...", AppLanguage.PA to "ਆਪਣਾ ਖੇਤੀ ਸਵਾਲ ਲਿਖੋ..."),
        "send" to mapOf(AppLanguage.EN to "Send", AppLanguage.HI to "भेजें", AppLanguage.PA to "ਭੇਜੋ"),
        "chip_1" to mapOf(AppLanguage.EN to "Best fertilizer for Wheat?", AppLanguage.HI to "गेहूं के लिए सबसे अच्छी खाद?", AppLanguage.PA to "ਕਣਕ ਲਈ ਸਭ ਤੋਂ ਵਧੀਆ ਖਾਦ?"),
        "chip_2" to mapOf(AppLanguage.EN to "How to control Rice stem borer?", AppLanguage.HI to "धान के तना छेदक को कैसे रोकें?", AppLanguage.PA to "ਝੋਨੇ ਦੇ ਤਣੇ ਦੇ ਕੀੜੇ ਨੂੰ ਕਿਵੇਂ ਰੋਕਿਆ ਜਾਵੇ?"),
        "chip_3" to mapOf(AppLanguage.EN to "How to test soil moisture?", AppLanguage.HI to "मिट्टी की नमी का परीक्षण कैसे करें?", AppLanguage.PA to "ਮਿੱਟੀ ਦੀ ਨਮੀ ਦੀ ਜਾਂਚ ਕਿਵੇਂ ਕਰੀਏ?"),

        // Farm
        "my_farm" to mapOf(AppLanguage.EN to "My Farm", AppLanguage.HI to "मेरा फार्म", AppLanguage.PA to "ਮੇਰਾ ਫਾਰਮ"),
        "add_field" to mapOf(AppLanguage.EN to "+ Add Field", AppLanguage.HI to "+ खेत जोड़ें", AppLanguage.PA to "+ ਖੇਤ ਜੋੜੋ"),
        "no_farms_yet" to mapOf(AppLanguage.EN to "No fields added yet", AppLanguage.HI to "अभी तक कोई खेत नहीं जोड़ा गया", AppLanguage.PA to "ਅਜੇ ਤੱਕ ਕੋਈ ਖੇਤ ਨਹੀਂ ਜੋੜਿਆ ਗਿਆ"),
        "add_first_farm" to mapOf(AppLanguage.EN to "Add Your First Farm", AppLanguage.HI to "अपना पहला खेत जोड़ें", AppLanguage.PA to "ਆਪਣਾ ਪਹਿਲਾ ਖੇਤ ਜੋੜੋ"),
        "todays_tasks" to mapOf(AppLanguage.EN to "Today's Tasks", AppLanguage.HI to "आज के कार्य", AppLanguage.PA to "ਅੱਜ ਦੇ ਕੰਮ"),
        "add_task" to mapOf(AppLanguage.EN to "Add Task", AppLanguage.HI to "कार्य जोड़ें", AppLanguage.PA to "ਕੰਮ ਜੋੜੋ"),
        "status_growing" to mapOf(AppLanguage.EN to "Growing", AppLanguage.HI to "बढ़ रही है", AppLanguage.PA to "ਵਾਧਾ"),
        "status_sown" to mapOf(AppLanguage.EN to "Sown", AppLanguage.HI to "बोई गई", AppLanguage.PA to "ਬੀਜੀ"),
        "status_preparing" to mapOf(AppLanguage.EN to "Preparing", AppLanguage.HI to "तैयारी", AppLanguage.PA to "ਤਿਆਰੀ"),

        // Guides
        "guides_title" to mapOf(AppLanguage.EN to "Farming Guides", AppLanguage.HI to "कृषि गाइड्स", AppLanguage.PA to "ਖੇਤੀਬਾੜੀ ਗਾਈਡਾਂ"),
        "search_guides" to mapOf(AppLanguage.EN to "Search guides...", AppLanguage.HI to "गाइड खोजें...", AppLanguage.PA to "ਗਾਈਡਾਂ ਲੱਭੋ..."),
        "category_all" to mapOf(AppLanguage.EN to "All", AppLanguage.HI to "सभी", AppLanguage.PA to "ਸਾਰੇ"),
        "category_crops" to mapOf(AppLanguage.EN to "Crops", AppLanguage.HI to "फसलें", AppLanguage.PA to "ਫ਼ਸਲਾਂ"),
        "category_fertilizer" to mapOf(AppLanguage.EN to "Fertilizer", AppLanguage.HI to "उर्वरक", AppLanguage.PA to "ਖਾਦ"),
        "category_irrigation" to mapOf(AppLanguage.EN to "Irrigation", AppLanguage.HI to "सिंचाई", AppLanguage.PA to "ਸਿੰਚਾਈ"),
        "category_pests" to mapOf(AppLanguage.EN to "Pest Control", AppLanguage.HI to "कीट नियंत्रण", AppLanguage.PA to "ਕੀੜੇ-ਮਕੌੜੇ ਕੰਟਰੋਲ"),

        // Profile & Settings
        "profile_title" to mapOf(AppLanguage.EN to "Profile", AppLanguage.HI to "प्रोफ़ाइल", AppLanguage.PA to "ਪ੍ਰੋਫਾਈਲ"),
        "edit_profile" to mapOf(AppLanguage.EN to "Edit Profile", AppLanguage.HI to "प्रोफ़ाइल संपादित करें", AppLanguage.PA to "ਪ੍ਰੋਫਾਈਲ ਸੰਪਾਦਿਤ ਕਰੋ"),
        "settings_title" to mapOf(AppLanguage.EN to "Settings", AppLanguage.HI to "सेटिंग्स", AppLanguage.PA to "ਸੈਟਿੰਗਾਂ"),
        "save_changes" to mapOf(AppLanguage.EN to "Save Changes", AppLanguage.HI to "सहेजें", AppLanguage.PA to "ਸੰਭਾਲੋ"),
        "full_name" to mapOf(AppLanguage.EN to "Name", AppLanguage.HI to "नाम", AppLanguage.PA to "ਨਾਮ"),
        "farm_name" to mapOf(AppLanguage.EN to "Farm Name", AppLanguage.HI to "फार्म का नाम", AppLanguage.PA to "ਫਾਰਮ ਦਾ ਨਾਮ"),
        "farm_details" to mapOf(AppLanguage.EN to "Farm Details", AppLanguage.HI to "फार्म विवरण", AppLanguage.PA to "ਫਾਰਮ ਵੇਰਵੇ"),
        "farm_size" to mapOf(AppLanguage.EN to "Farm Size (Acres)", AppLanguage.HI to "खेत का आकार (एकड़)", AppLanguage.PA to "ਖੇਤ ਦਾ ਆਕਾਰ (ਏਕੜ)"),
        "location" to mapOf(AppLanguage.EN to "Location", AppLanguage.HI to "स्थान", AppLanguage.PA to "ਸਥਾਨ"),
        "phone" to mapOf(AppLanguage.EN to "Phone Number", AppLanguage.HI to "फ़ोन नंबर", AppLanguage.PA to "ਫੋਨ ਨੰਬਰ"),
        "primary_crop" to mapOf(AppLanguage.EN to "Primary Crop", AppLanguage.HI to "मुख्य फसल", AppLanguage.PA to "ਮੁੱਖ ਫ਼ਸਲ"),
        "main_crop" to mapOf(AppLanguage.EN to "Main Crop", AppLanguage.HI to "मुख्य फसल", AppLanguage.PA to "ਮੁੱਖ ਫ਼ਸਲ"),
        "other_crops" to mapOf(AppLanguage.EN to "Other Crops", AppLanguage.HI to "अन्य फसलें", AppLanguage.PA to "ਹੋਰ ਫ਼ਸਲਾਂ"),
        "irrigation_type" to mapOf(AppLanguage.EN to "Irrigation Type", AppLanguage.HI to "सिंचाई का प्रकार", AppLanguage.PA to "ਸਿੰਚਾਈ ਦੀ ਕਿਸਮ"),
        "experience" to mapOf(AppLanguage.EN to "Farming Experience (Years)", AppLanguage.HI to "खेती का अनुभव (वर्ष)", AppLanguage.PA to "ਖੇਤੀ ਦਾ ਤਜਰਬਾ (ਸਾਲ)"),
        "experience_years" to mapOf(AppLanguage.EN to "Experience Years", AppLanguage.HI to "अनुभव वर्ष", AppLanguage.PA to "ਤਜਰਬੇ ਦੇ ਸਾਲ"),
        "years" to mapOf(AppLanguage.EN to "Years", AppLanguage.HI to "वर्ष", AppLanguage.PA to "ਸਾਲ"),
        "preferred_language" to mapOf(AppLanguage.EN to "Language", AppLanguage.HI to "भाषा", AppLanguage.PA to "ਭਾਸ਼ਾ"),
        "unnamed_farmer" to mapOf(AppLanguage.EN to "Set Your Name", AppLanguage.HI to "अपना नाम दर्ज करें", AppLanguage.PA to "ਆਪਣਾ ਨਾਂ ਦਰਜ ਕਰੋ"),
        "not_set" to mapOf(AppLanguage.EN to "Not set", AppLanguage.HI to "सेट नहीं है", AppLanguage.PA to "ਸੈੱਟ ਨਹੀਂ"),
        "crop_history" to mapOf(AppLanguage.EN to "Crop History", AppLanguage.HI to "फसल का इतिहास", AppLanguage.PA to "ਫ਼ਸਲ ਦਾ ਇਤਿਹਾਸ"),
        "saved_recommendations" to mapOf(AppLanguage.EN to "Saved Recommendations", AppLanguage.HI to "सहेजी गई सिफारिशें", AppLanguage.PA to "ਸੰਭਾਲੀਆਂ ਸਿਫ਼ਾਰਸ਼ਾਂ"),
        "notifications" to mapOf(AppLanguage.EN to "Notifications", AppLanguage.HI to "सूचनाएं", AppLanguage.PA to "ਨੋਟੀਫਿਕੇਸ਼ਨਾਂ"),
        "help_support" to mapOf(AppLanguage.EN to "Help & Support", AppLanguage.HI to "सहायता और समर्थन", AppLanguage.PA to "ਮਦਦ ਅਤੇ ਸਹਾਇਤਾ"),

        // Authentication & Onboarding
        "welcome_tagline" to mapOf(
            AppLanguage.EN to "Smart AI-powered farming assistance, crop insights, disease diagnosis, and farm management for modern farmers.",
            AppLanguage.HI to "आधुनिक किसानों के लिए स्मार्ट AI-संचालित कृषि सहायता, फसल सुझाव, रोग निदान और खेत प्रबंधन।",
            AppLanguage.PA to "ਆਧੁਨਿਕ ਕਿਸਾਨਾਂ ਲਈ ਸਮਾਰਟ AI-ਸੰਚਾਲਿਤ ਖੇਤੀਬਾੜੀ ਸਹਾਇਤਾ, ਫ਼ਸਲ ਸੁਝਾਅ, ਬਿਮਾਰੀ ਜਾਂਚ ਅਤੇ ਖੇਤ ਪ੍ਰਬੰਧਨ।"
        ),
        "get_started" to mapOf(AppLanguage.EN to "Get Started", AppLanguage.HI to "शुरू करें", AppLanguage.PA to "ਸ਼ੁਰੂ ਕਰੋ"),
        "log_in" to mapOf(AppLanguage.EN to "Log In", AppLanguage.HI to "लॉग इन करें", AppLanguage.PA to "ਲਾਗਇਨ ਕਰੋ"),
        "create_account" to mapOf(AppLanguage.EN to "Create Account", AppLanguage.HI to "खाता बनाएं", AppLanguage.PA to "ਖਾਤਾ ਬਣਾਓ"),
        "email" to mapOf(AppLanguage.EN to "Email", AppLanguage.HI to "ईमेल", AppLanguage.PA to "ਈਮੇਲ"),
        "password" to mapOf(AppLanguage.EN to "Password", AppLanguage.HI to "पासवर्ड", AppLanguage.PA to "ਪਾਸਵਰਡ"),
        "confirm_password" to mapOf(AppLanguage.EN to "Confirm Password", AppLanguage.HI to "पासवर्ड की पुष्टि करें", AppLanguage.PA to "ਪਾਸਵਰਡ ਦੀ ਪੁਸ਼ਟੀ ਕਰੋ"),
        "already_have_account" to mapOf(AppLanguage.EN to "Already have an account? Log In", AppLanguage.HI to "क्या आपके पास पहले से खाता है? लॉग इन करें", AppLanguage.PA to "ਕੀ ਤੁਹਾਡੇ ਕੋਲ ਪਹਿਲਾਂ ਹੀ ਖਾਤਾ ਹੈ? ਲਾਗਇਨ ਕਰੋ"),
        "dont_have_account" to mapOf(AppLanguage.EN to "Don't have an account? Create Account", AppLanguage.HI to "खाता नहीं है? खाता बनाएं", AppLanguage.PA to "ਕੋਈ ਖਾਤਾ ਨਹੀਂ ਹੈ? ਖਾਤਾ ਬਣਾਓ"),
        "setup_title" to mapOf(AppLanguage.EN to "Farm Setup", AppLanguage.HI to "खेत सेटअप", AppLanguage.PA to "ਖੇਤ ਸੈੱਟਅੱਪ"),
        "setup_subtitle" to mapOf(AppLanguage.EN to "Tell us a bit about yourself and your farm", AppLanguage.HI to "हमें अपने और अपने खेत के बारे में कुछ बताएं", AppLanguage.PA to "ਸਾਨੂੰ ਆਪਣੇ ਅਤੇ ਆਪਣੇ ਖੇਤ ਬਾਰੇ ਕੁਝ ਦੱਸੋ"),
        "detect_location" to mapOf(AppLanguage.EN to "Detect GPS Location", AppLanguage.HI to "GPS स्थान का पता लगाएं", AppLanguage.PA to "GPS ਸਥਾਨ ਦਾ ਪਤਾ ਲਗਾਓ"),
        "complete_setup" to mapOf(AppLanguage.EN to "Complete Setup", AppLanguage.HI to "सेटअप पूरा करें", AppLanguage.PA to "ਸੈੱਟਅੱਪ ਪੂਰਾ ਕਰੋ"),
        "logout" to mapOf(AppLanguage.EN to "Log Out", AppLanguage.HI to "लॉग आउट", AppLanguage.PA to "ਲਾਗ ਆਉਟ"),
        "logout_confirm" to mapOf(AppLanguage.EN to "Are you sure you want to log out of this account?", AppLanguage.HI to "क्या आप वाकई इस खाते से लॉग आउट करना चाहते हैं?", AppLanguage.PA to "ਕੀ ਤੁਸੀਂ ਯਕੀਨੀ ਤੌਰ 'ਤੇ ਇਸ ਖਾਤੇ ਤੋਂ ਲਾਗ ਆਉਟ ਕਰਨਾ ਚਾਹੁੰਦੇ ਹੋ?"),
        "no_farm_added_yet" to mapOf(AppLanguage.EN to "No farm added yet", AppLanguage.HI to "अभी तक कोई खेत नहीं जोड़ा गया", AppLanguage.PA to "ਅਜੇ ਤੱਕ ਕੋਈ ਖੇਤ ਨਹੀਂ ਜੋੜਿਆ ਗਿਆ"),
        "add_your_farm" to mapOf(AppLanguage.EN to "Add Your Farm", AppLanguage.HI to "अपना खेत जोड़ें", AppLanguage.PA to "ਆਪਣਾ ਖੇਤ ਜੋੜੋ"),

        // Settings Sections
        "app_preferences" to mapOf(AppLanguage.EN to "APP PREFERENCES", AppLanguage.HI to "ऐप प्राथमिकताएं", AppLanguage.PA to "ਐਪ ਤਰਜੀਹਾਂ"),
        "theme" to mapOf(AppLanguage.EN to "Theme", AppLanguage.HI to "थीम", AppLanguage.PA to "ਥੀਮ"),
        "weather_alerts" to mapOf(AppLanguage.EN to "Weather Alerts", AppLanguage.HI to "मौसम अलर्ट", AppLanguage.PA to "ਮੌਸਮ ਅਲਰਟ"),
        "farming_reminders" to mapOf(AppLanguage.EN to "Farming Reminders", AppLanguage.HI to "खेती के रिमाइंडर", AppLanguage.PA to "ਖੇਤੀ ਦੇ ਰਿਮਾਈਂਡਰ"),
        "crop_updates" to mapOf(AppLanguage.EN to "Crop Updates", AppLanguage.HI to "फसल अपडेट", AppLanguage.PA to "ਫ਼ਸਲ ਅੱਪਡੇਟ"),
        "account" to mapOf(AppLanguage.EN to "ACCOUNT", AppLanguage.HI to "खाता", AppLanguage.PA to "ਖਾਤਾ"),
        "change_password" to mapOf(AppLanguage.EN to "Change Password", AppLanguage.HI to "पासवर्ड बदलें", AppLanguage.PA to "ਪਾਸਵਰਡ ਬਦਲੋ"),
        "delete_account" to mapOf(AppLanguage.EN to "Delete Account", AppLanguage.HI to "खाता हटाएं", AppLanguage.PA to "ਖਾਤਾ ਹਟਾਓ"),
        "about" to mapOf(AppLanguage.EN to "ABOUT", AppLanguage.HI to "के बारे में", AppLanguage.PA to "ਬਾਰੇ"),
        "app_version" to mapOf(AppLanguage.EN to "App Version", AppLanguage.HI to "ऐप का संस्करण", AppLanguage.PA to "ਐਪ ਦਾ ਵਰਜ਼ਨ"),

        // General UI States
        "loading" to mapOf(AppLanguage.EN to "Loading...", AppLanguage.HI to "लोड हो रहा है...", AppLanguage.PA to "ਲੋਡ ਹੋ ਰਿਹਾ ਹੈ..."),
        "error" to mapOf(AppLanguage.EN to "Something went wrong", AppLanguage.HI to "कुछ गलत हो गया", AppLanguage.PA to "ਕੁਝ ਗਲਤ ਹੋ ਗਿਆ"),
        "retry" to mapOf(AppLanguage.EN to "Retry", AppLanguage.HI to "पुनः प्रयास करें", AppLanguage.PA to "ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ"),
        "cancel" to mapOf(AppLanguage.EN to "Cancel", AppLanguage.HI to "रद्द करें", AppLanguage.PA to "ਰੱਦ ਕਰੋ"),
        "field_name" to mapOf(AppLanguage.EN to "Field Name", AppLanguage.HI to "खेत का नाम", AppLanguage.PA to "ਖੇਤ ਦਾ ਨਾਂ"),
        "crop_name" to mapOf(AppLanguage.EN to "Crop Name", AppLanguage.HI to "फसल का नाम", AppLanguage.PA to "ਫ਼ਸਲ ਦਾ ਨਾਂ"),
        "error_rate_limit" to mapOf(
            AppLanguage.EN to "AI service is temporarily unavailable. Please try again shortly.",
            AppLanguage.HI to "AI सेवा अस्थायी रूप से अनुपलब्ध है। कृपया कुछ देर बाद पुनः प्रयास करें।",
            AppLanguage.PA to "AI ਸੇਵਾ ਅਸਥਾਈ ਤੌਰ 'ਤੇ ਉਪਲਬਧ ਨਹੀਂ ਹੈ। ਕਿਰਪਾ ਕਰਕੇ ਕੁਝ ਸਮੇਂ ਬਾਅਦ ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ।"
        ),
        "error_auth" to mapOf(
            AppLanguage.EN to "AI service configuration error.",
            AppLanguage.HI to "AI सेवा कॉन्फ़िगरेशन त्रुटि।",
            AppLanguage.PA to "AI ਸੇਵਾ ਕੌਂਫਿਗਰੇਸ਼ਨ ਤਰੁੱਟੀ।"
        ),
        "error_permission" to mapOf(
            AppLanguage.EN to "AI service is currently unavailable.",
            AppLanguage.HI to "AI सेवा वर्तमान में अनुपलब्ध है।",
            AppLanguage.PA to "AI ਸੇਵਾ ਵਰਤਮਾਨ ਵਿੱਚ ਉਪਲਬਧ ਨਹੀਂ ਹੈ।"
        ),
        "error_timeout" to mapOf(
            AppLanguage.EN to "The request timed out. Please try again.",
            AppLanguage.HI to "अनुरोध का समय समाप्त हो गया। कृपया पुनः प्रयास करें।",
            AppLanguage.PA to "ਬੇਨਤੀ ਦਾ ਸਮਾਂ ਸਮਾਪਤ ਹੋ ਗਿਆ। ਕਿਰਪਾ ਕਰਕੇ ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ।"
        ),
        "error_server" to mapOf(
            AppLanguage.EN to "AI service is temporarily unavailable.",
            AppLanguage.HI to "AI सेवा अस्थायी रूप से अनुपलब्ध है।",
            AppLanguage.PA to "AI ਸੇਵਾ ਅਸਥਾਈ ਤੌਰ 'ਤੇ ਉਪਲਬਧ ਨਹੀਂ ਹੈ।"
        ),
        "error_no_internet" to mapOf(
            AppLanguage.EN to "No internet connection. Please check your connection.",
            AppLanguage.HI to "कोई इंटरनेट कनेक्शन नहीं है। कृपया अपना कनेक्शन जांचें।",
            AppLanguage.PA to "ਕੋਈ ਇੰਟਰਨੈੱਟ ਕਨੈਕਸ਼ਨ ਨਹੀਂ ਹੈ। ਕਿਰਪਾ ਕਰਕੇ ਆਪਣਾ ਕਨੈਕਸ਼ਨ ਚੈੱਕ ਕਰੋ।"
        ),
        "error_key_missing" to mapOf(
            AppLanguage.EN to "Gemini API key is missing. Please add it in secrets.",
            AppLanguage.HI to "Gemini API कुंजी गायब है। कृपया इसे सीक्रेट्स में जोड़ें।",
            AppLanguage.PA to "Gemini API ਕੁੰਜੀ ਗੁੰਮ ਹੈ। ਕਿਰਪਾ ਕਰਕੇ ਇਸਨੂੰ ਸੀਕ੍ਰੇਟਸ ਵਿੱਚ ਸ਼ਾਮਲ ਕਰੋ।"
        )
    )

    fun getString(key: String, language: AppLanguage): String {
        return translations[key]?.get(language) ?: translations[key]?.get(AppLanguage.EN) ?: key
    }
}

enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING
}

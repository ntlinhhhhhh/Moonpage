package com.diary.moonpage.core.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.DrawableRes
import com.diary.moonpage.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

data class MoonIcon(
    val vector: ImageVector? = null,
    val color: Color,
    val name: String = "",
    @DrawableRes val drawableRes: Int? = null,
    val imageUrl: String? = null
)

object MoonIcons {

    object Moods {
        val VeryHappy = MoonIcon(null, Color.Unspecified, "Very Happy", R.drawable.very_happy)
        val Happy = MoonIcon(null, Color.Unspecified, "Happy", R.drawable.happy) 
        val Neutral = MoonIcon(null, Color.Unspecified, "Neutral", R.drawable.neutral) 
        val Sad = MoonIcon(null, Color.Unspecified, "Sad", R.drawable.sad) 
        val VerySad = MoonIcon(null, Color.Unspecified, "Very Sad", R.drawable.very_sad)

        fun getMoodColor(
            level: Int, 
            themeType: com.diary.moonpage.core.theme.MoonThemeType,
            customMoods: Map<Int, MoonIcon>? = null
        ): Color {
            if (customMoods != null && customMoods.containsKey(level)) {
                return customMoods[level]!!.color
            }
            val shades = com.diary.moonpage.core.theme.getThemeShades(themeType)
            return when (level) {
                1 -> shades[4]
                2 -> shades[3]
                3 -> shades[2]
                4 -> shades[1]
                5 -> shades[0]
                else -> shades[2]
            }
        }

        fun getMoodVisual(
            level: Int, 
            themeType: com.diary.moonpage.core.theme.MoonThemeType,
            customMoods: Map<Int, MoonIcon>? = null
        ): MoonIcon {
            if (customMoods != null && customMoods.containsKey(level)) {
                return customMoods[level]!!
            }
            val color = getMoodColor(level, themeType)
            return when (level) {
                1 -> VerySad.copy(color = color)
                2 -> Sad.copy(color = color)
                3 -> Neutral.copy(color = color)
                4 -> Happy.copy(color = color)
                5 -> VeryHappy.copy(color = color)
                else -> Neutral.copy(color = color)
            }
        }
    }

    object Hobbies {
        val Exercise = MoonIcon(Icons.Rounded.FitnessCenter, Color(0xFFE53935), "Exercise")
        val TvContent = MoonIcon(Icons.Rounded.Tv, Color(0xFF2C82C4), "TV & Content")
        val Movie = MoonIcon(Icons.Rounded.Movie, Color(0xFF3D5AFE), "Movie")
        val Gaming = MoonIcon(Icons.Rounded.SportsEsports, Color(0xFF0091EA), "Gaming")
        val Reading = MoonIcon(Icons.Rounded.AutoStories, Color(0xFF6D4C41), "Reading")
        val Walk = MoonIcon(Icons.AutoMirrored.Rounded.DirectionsWalk, Color(0xFF2E7D32), "Walk")
        val Music = MoonIcon(Icons.Rounded.MusicNote, Color(0xFFF50057), "Music")
        val Drawing = MoonIcon(Icons.Rounded.Brush, Color(0xFFFF8F00), "Drawing")
    }

    object Emotions {
        val Excited = MoonIcon(Icons.Rounded.Celebration, Color(0xFFFFB300), "Excited")
        val Relaxed = MoonIcon(Icons.Rounded.Spa, Color(0xFF4CAF50), "Relaxed")
        val Proud = MoonIcon(Icons.Rounded.EmojiEvents, Color(0xFFFF8F00), "Proud")
        val Hopeful = MoonIcon(Icons.Rounded.AutoAwesome, Color(0xFFFFCA28), "Hopeful")
        val Happy = MoonIcon(Icons.Rounded.SentimentVerySatisfied, Color(0xFFFFB300), "Happy")
        val Enthusiastic = MoonIcon(Icons.Rounded.Whatshot, Color(0xFFE64A19), "Enthusiastic")
        val PitAPat = MoonIcon(Icons.Rounded.Favorite, Color(0xFFF06292), "Pit-a-pat")
        val Refreshed = MoonIcon(Icons.Rounded.WaterDrop, Color(0xFF039BE5), "Refreshed")
        val Calm = MoonIcon(Icons.Rounded.SelfImprovement, Color(0xFF7E57C2), "Calm")
        val Grateful = MoonIcon(Icons.Rounded.VolunteerActivism, Color(0xFFE91E63), "Grateful")
        val Depressed = MoonIcon(Icons.Rounded.SentimentVeryDissatisfied, Color(0xFF3949AB), "Depressed")
        val Lonely = MoonIcon(Icons.Rounded.PersonOutline, Color(0xFF4C9FBD), "Lonely")
        val Anxious = MoonIcon(Icons.Rounded.SentimentDissatisfied, Color(0xFF0F87BD), "Anxious")
        val Sad = MoonIcon(Icons.Rounded.MoodBad, Color(0xFF1B75D0), "Sad")
        val Angry = MoonIcon(Icons.Rounded.PriorityHigh, Color(0xFFD32F2F), "Angry")
        val Pressured = MoonIcon(Icons.Rounded.Timer, Color(0xFFE64A19), "Pressured")
        val Annoyed = MoonIcon(Icons.Rounded.ErrorOutline, Color(0xFFF4511E), "Annoyed")
        val Tired = MoonIcon(Icons.Rounded.Battery0Bar, Color(0xFF5D4037), "Tired")
        val Stressed = MoonIcon(Icons.Rounded.Psychology, Color(0xFF3F51B5), "Stressed")
        val Bored = MoonIcon(Icons.Rounded.SentimentNeutral, Color(0xFFC55113), "Bored")
    }

    object Meals {
        val Breakfast = MoonIcon(Icons.Rounded.BreakfastDining, Color(0xFFFFB300), "Breakfast")
        val Lunch = MoonIcon(Icons.Rounded.LunchDining, Color(0xFFF4511E), "Lunch")
        val Dinner = MoonIcon(Icons.Rounded.DinnerDining, Color(0xFFD84315), "Dinner")
        val NightSnack = MoonIcon(Icons.Rounded.Nightlight, Color(0xFF3949AB), "Night Snack")
    }

    object SelfCare {
        val Shower = MoonIcon(Icons.Rounded.Shower, Color(0xFF0288D1), "Shower")
        val BrushTeeth = MoonIcon(Icons.Rounded.CleanHands, Color(0xFF039BE5), "Brush Teeth")
        val WashFace = MoonIcon(Icons.Rounded.WaterDrop, Color(0xFF29B6F6), "Wash Face")
        val DrinkWater = MoonIcon(Icons.Rounded.LocalDrink, Color(0xFF0277BD), "Drink Water")
    }

    object Chores {
        val Cleaning = MoonIcon(Icons.Rounded.CleaningServices, Color(0xFF7CB342), "Cleaning")
        val Cooking = MoonIcon(Icons.Rounded.Restaurant, Color(0xFFFB8C00), "Cooking")
        val Laundry = MoonIcon(Icons.Rounded.LocalLaundryService, Color(0xFF1E88E5), "Laundry")
        val Dishes = MoonIcon(Icons.Rounded.Countertops, Color(0xFF00897B), "Dishes")
    }

    object Events {
        val StayHome = MoonIcon(Icons.Rounded.Home, Color(0xFF7E57C2), "Stay Home")
        val School = MoonIcon(Icons.Rounded.School, Color(0xFF3949AB), "School")
        val Restaurant = MoonIcon(Icons.Rounded.Restaurant, Color(0xFFF4511E), "Restaurant")
        val Cafe = MoonIcon(Icons.Rounded.Coffee, Color(0xFF6D4C41), "Cafe")
        val Shopping = MoonIcon(Icons.Rounded.ShoppingBag, Color(0xFFD81B60), "Shopping")
        val Travel = MoonIcon(Icons.Rounded.TravelExplore, Color(0xFF2E7D32), "Travel")
        val Party = MoonIcon(Icons.Rounded.Celebration, Color(0xFFF50057), "Party")
        val Cinema = MoonIcon(Icons.Rounded.Theaters, Color(0xFF3F90B5), "Cinema")
    }

    object People {
        val Friends = MoonIcon(Icons.Rounded.Group, Color(0xFF00897B), "Friends")
        val Family = MoonIcon(Icons.Rounded.Groups, Color(0xFFD81B60), "Family")
        val Partner = MoonIcon(Icons.Rounded.Favorite, Color(0xFFC2185B), "Partner")
        val None = MoonIcon(Icons.Rounded.PersonOff, Color(0xFF2196F3), "None")
    }

    object Beauty {
        val Hair = MoonIcon(Icons.Rounded.ContentCut, Color(0xFFAB47BC), "Hair")
        val Nails = MoonIcon(Icons.Rounded.Palette, Color(0xFFEC407A), "Nails")
        val Skincare = MoonIcon(Icons.Rounded.FaceRetouchingNatural, Color(0xFFF06292), "Skincare")
        val Makeup = MoonIcon(Icons.Rounded.AutoFixHigh, Color(0xFFBA68C8), "Makeup")
    }

    object Weather {
        val Sunny = MoonIcon(Icons.Rounded.WbSunny, Color(0xFFFFB300), "Sunny")
        val Cloudy = MoonIcon(Icons.Rounded.Cloud, Color(0xFF78909C), "Cloudy")
        val Rainy = MoonIcon(Icons.Rounded.Umbrella, Color(0xFF1E88E5), "Rainy")
        val Snowy = MoonIcon(Icons.Rounded.AcUnit, Color(0xFF039BE5), "Snowy")
        val Windy = MoonIcon(Icons.Rounded.Air, Color(0xFF4CAF50), "Windy")
        val Stormy = MoonIcon(Icons.Rounded.Thunderstorm, Color(0xFFFF9800), "Stormy")
        val Hot = MoonIcon(Icons.Rounded.Thermostat, Color(0xFFE64A19), "Hot")
        val Cold = MoonIcon(Icons.Rounded.SevereCold, Color(0xFF01579B), "Cold")
    }

    object Health {
        val Sick = MoonIcon(Icons.Rounded.Sick, Color(0xFFAFB42B), "Sick")
        val Hospital = MoonIcon(Icons.Rounded.LocalHospital, Color(0xFFD32F2F), "Hospital")
        val Checkup = MoonIcon(Icons.Rounded.AssignmentTurnedIn, Color(0xFF388E3C), "Checkup")
        val Medicine = MoonIcon(Icons.Rounded.Medication, Color(0xFF0288D1), "Medicine")
    }

    object Work {
        val Work = MoonIcon(Icons.Rounded.Work, Color(0xFF3949AB), "Work")
        val EndOnTime = MoonIcon(Icons.Rounded.AlarmOn, Color(0xFF388E3C), "End on Time")
        val Overtime = MoonIcon(Icons.Rounded.AccessTime, Color(0xFFF4511E), "Overtime")
        val Vacation = MoonIcon(Icons.Rounded.BeachAccess, Color(0xFF00897B), "Vacation")
    }

    object Other {
        val Snack = MoonIcon(Icons.Rounded.Cookie, Color(0xFFFB8C00), "Snack")
        val Coffee = MoonIcon(Icons.Rounded.Coffee, Color(0xFF6D4C41), "Coffee")
        val Beverage = MoonIcon(Icons.Rounded.LocalDrink, Color(0xFF0288D1), "Beverage")
        val Tea = MoonIcon(Icons.Rounded.EmojiFoodBeverage, Color(0xFF7CB342), "Tea")
        val Alcohol = MoonIcon(Icons.Rounded.LocalBar, Color(0xFF7E57C2), "Alcohol")
        val Smoking = MoonIcon(Icons.Rounded.SmokingRooms, Color(0xFF2196F3), "Smoking")
    }

    object School {
        val Class = MoonIcon(Icons.Rounded.CastForEducation, Color(0xFF3949AB), "Class")
        val Study = MoonIcon(Icons.Rounded.AutoStories, Color(0xFF3F51B5), "Study")
        val Homework = MoonIcon(Icons.Rounded.EditNote, Color(0xFF5C6BC0), "Homework")
        val Exam = MoonIcon(Icons.AutoMirrored.Rounded.FactCheck, Color(0xFFD32F2F), "Exam")
    }

    object Relationship {
        val Date = MoonIcon(Icons.Rounded.Favorite, Color(0xFFD81B60), "Date")
        val Anniversary = MoonIcon(Icons.Rounded.Cake, Color(0xFFE64A19), "Anniversary")
        val Gift = MoonIcon(Icons.Rounded.CardGiftcard, Color(0xFFFFB300), "Gift")
        val Conflict = MoonIcon(Icons.Rounded.FlashOn, Color(0xFF00BCD4), "Conflict")
        val Sex = MoonIcon(Icons.Rounded.BedroomParent, Color(0xFF8E24AA), "Sex")
    }

    fun getAllCategories(): Map<String, List<MoonIcon>> {
        return mapOf(
            "Moods" to listOf(Moods.VeryHappy, Moods.Happy, Moods.Neutral, Moods.Sad, Moods.VerySad),
            "Hobbies" to listOf(Hobbies.Exercise, Hobbies.TvContent, Hobbies.Movie, Hobbies.Gaming, Hobbies.Reading, Hobbies.Walk, Hobbies.Music, Hobbies.Drawing),
            "Emotions" to listOf(Emotions.Excited, Emotions.Relaxed, Emotions.Proud, Emotions.Hopeful, Emotions.Happy, Emotions.Enthusiastic, Emotions.PitAPat, Emotions.Refreshed, Emotions.Calm, Emotions.Grateful, Emotions.Depressed, Emotions.Lonely, Emotions.Anxious, Emotions.Sad, Emotions.Angry, Emotions.Pressured, Emotions.Annoyed, Emotions.Tired, Emotions.Stressed, Emotions.Bored),
            "Meals" to listOf(Meals.Breakfast, Meals.Lunch, Meals.Dinner, Meals.NightSnack),
            "SelfCare" to listOf(SelfCare.Shower, SelfCare.BrushTeeth, SelfCare.WashFace, SelfCare.DrinkWater),
            "Chores" to listOf(Chores.Cleaning, Chores.Cooking, Chores.Laundry, Chores.Dishes),
            "Events" to listOf(Events.StayHome, Events.School, Events.Restaurant, Events.Cafe, Events.Shopping, Events.Travel, Events.Party, Events.Cinema),
            "People" to listOf(People.Friends, People.Family, People.Partner, People.None),
            "Beauty" to listOf(Beauty.Hair, Beauty.Nails, Beauty.Skincare, Beauty.Makeup),
            "Weather" to listOf(Weather.Sunny, Weather.Cloudy, Weather.Rainy, Weather.Snowy, Weather.Windy, Weather.Stormy, Weather.Hot, Weather.Cold),
            "Health" to listOf(Health.Sick, Health.Hospital, Health.Checkup, Health.Medicine),
            "Work" to listOf(Work.Work, Work.EndOnTime, Work.Overtime, Work.Vacation),
            "Other" to listOf(Other.Snack, Other.Coffee, Other.Beverage, Other.Tea, Other.Alcohol, Other.Smoking),
            "School" to listOf(School.Class, School.Study, School.Homework, School.Exam),
            "Relationship" to listOf(Relationship.Date, Relationship.Anniversary, Relationship.Gift, Relationship.Conflict, Relationship.Sex)
        )
    }

    val allIconsList: List<MoonIcon> by lazy {
        getAllCategories().values.flatten()
    }

    private val iconMapByName: Map<String, MoonIcon> by lazy {
        allIconsList.associateBy { it.name.replace(" ", "").lowercase() }
    }

    fun getAllIcons(): List<MoonIcon> = allIconsList

    fun getIconForActivity(activityName: String): MoonIcon {
        val searchKey = activityName.replace(" ", "").lowercase()
        return iconMapByName[searchKey] ?: Other.Coffee
    }

    fun getWeatherIconVector(weatherText: String): ImageVector? {
        val cleanName = weatherText.split(" ")[0].lowercase()
        return when {
            cleanName.contains("sunny") -> Weather.Sunny.vector
            cleanName.contains("cloudy") -> Weather.Cloudy.vector
            cleanName.contains("rainy") -> Weather.Rainy.vector
            cleanName.contains("snowy") -> Weather.Snowy.vector
            cleanName.contains("windy") -> Weather.Windy.vector
            cleanName.contains("stormy") -> Weather.Stormy.vector
            cleanName.contains("hot") -> Weather.Hot.vector
            cleanName.contains("cold") -> Weather.Cold.vector
            else -> null
        }
    }
}

package com.example.grimmy.Retrofit.Response

import com.google.gson.annotations.SerializedName

data class GoalGetResponse(
    @SerializedName("monthly_goals") val monthly_goals : List<MonthlyGoal>,
    @SerializedName("weekly_goals") val weekly_goals : List<WeeklyGoal>,
    @SerializedName("stuff") val stuff : List<Stuff>
)

data class MonthlyGoal(
    @SerializedName("goal_id") val goal_id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("subgoals") val subgoals : List<SubGoal>
)

data class WeeklyGoal(
    @SerializedName("goal_id") val goal_id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("subgoals") val subgoals : List<SubGoal>
)

data class SubGoal(
    @SerializedName("subgoal_id") val subgoal_id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("is_completed") val is_completed : Boolean
)

data class Stuff(
    @SerializedName("stuff_id") val stuff_id : Int,
    @SerializedName("title") val title: String,
    @SerializedName("substuffs") val substuffs : List<SubStuff>
)

data class SubStuff(
    @SerializedName("substuff_id") val substuff_id : Int,
    @SerializedName("title") val title : String,
    @SerializedName("is_completed") val is_completed: Boolean
)

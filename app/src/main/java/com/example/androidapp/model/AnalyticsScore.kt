// class to reflect overall final score for a user based on an analysis of their consumed and wasted food
enum class AnalyticsScore(val numericalScore: Int) {
    ENTIRELY_WASTEFUL(1), // 80%+ of inventory wasted
    MOSTLY_WASTEFUL(2), // 60%-79% of inventory wasted
    OVER_CONSUMER(3), // 30%-59%  of inventory wasted
    STANDARD_CONSUMER(4), // 15%-29%  of inventory wasted
    MINDFUL_CONSUMER(5), // 5%-14%  of inventory wasted
    ENTIRELY_SUSTAINABLE(6), // 0%-4%  of inventory wasted
}
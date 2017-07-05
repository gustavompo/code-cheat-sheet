  private val dateTimeWithTimeZonePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  
  implicit object bindableJodaWithTimeZoneDate extends PathParser[org.joda.time.DateTime](
    DateTimeFormat.forPattern(dateTimeWithTimeZonePattern).parseDateTime,
    _.toString(dateTimeWithTimeZonePattern),
    (key: String, e: Exception) => s"$key does not conform with the expected datetime pattern $dateTimeWithTimeZonePattern. e.g: 2017-06-06T12:31:00.000+0000"
  )
  val jdateTime = new PathBindableExtractor[org.joda.time.DateTime]
  
def routes = Router.from {
    case GET(p"/api/v1/safety/dexter/run?${jdateTime(date)}") => run
  }.routes

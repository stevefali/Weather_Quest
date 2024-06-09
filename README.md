# Weather_Quest


Thank you for taking the time to check out my code!

This app uses Android Jetpack Compose and modern Android libraries such as Hilt/Dagger, Room, Retrofit, and Moshi. 
Users can get weather data based on their device’s location or search for a location using the search bar. 
The search bar gives suggestions for locations as the user types. 
Once a location is chosen, the app fetches the weather data from a server and displays the current weather and forecast for the selected location. 
The UI features some animations, and the background image changes based on the current weather. 
Please give my app a look to see what I can do. 
The directory you want to look in (where the files containing most of my code are) is [app/src/main/java/com/steve/weatherquest](app/src/main/java/com/steve/weatherquest).

 It can also be downloaded from the Google Play Store [here](https://play.google.com/store/apps/details?id=com.steve.weatherquest).


 To run this project on your own, you would need to first obtain api keys from [OpenWeatherMap](https://openweathermap.org/api) and the [Here Api](https://developer.here.com).
 Those api keys can then be stored in the local.properties file as follows:
 ```
HERE_API_KEY="[Your_Api_Key]"
OPWEMA_API_KEY="[Your_Api_Key]"
```
(Note that the quotation marks are necessary.)

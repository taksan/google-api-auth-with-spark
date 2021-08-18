# Google Api Sample for Web Application in Java with Spark

This is a sample project that depicts how to implement the whole Google Api Oauth authentication to acquire a token
that can be used to access Google Api Resources.

Before running this example, make sure to get your credentials.json from Google Developer Console 
(see [https://developers.google.com/workspace/guides/create-credentials]) and place the credentials in the 
`src/main/resources` directory.

Just run and hit / to go the "login" screen for a run.

This specific example will grant you access to the primary calendar on the account you choose. The application
implements three main endpoints:

* /authentication?username=`username`
  * will "authenticate" the user in google. The username is arbitrary and is here just to "simulate" the username in
    your application
* /Callback 
  * After authentication on Google is succeeds, this is the endpoint to handle the result. This is where
    the return token will be stored and then be used for any api requests
* /list?username=`username`
  * This endpoint will list the next 10 upcoming events. The parameter is just there to simulate the "logged-in user"
    in your system. The endpoint will redirect to authentication if there are no tokens for the given user

This application is provided as an example of the whole cycle for web application. I spent sometime looking for
a whole example around, and I couldn't find a complete example, so when I finally got it working, I decided to
gather a full working sample. I hope it helps more people around.

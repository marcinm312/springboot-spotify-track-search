# Spotify Track Search Application

This application allows you to search for songs on the Spotify platform, using your Spotify account.

## Functionalities:
1. Searching for songs on Spotify;
2. Login to the app using your Spotify account.

## Used technologies and libraries:
1. Java
2. Maven
3. Spring Boot
4. Spring Security, OAuth2
5. Spotify API
6. Lombok
7. Spring Boot Starter Test, JUnit, MockMvc
8. Swagger
9. Actuator
10. HTML, JSP, CSS
11. Bootstrap

## Environment variables that need to be set:

|               Name               | Description                                                                                                                                                                                                                                                                                                                                                      |                 Example value                 | Default value |
|:--------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------:|:-------------:|
|      **LOGGING_FILE_NAME**       | Log file path and name. Names can be an exact location (for instance, `C://logs/server.log`) or relative (for instance, `logs/server.log`) to the current directory (project root directory or directory containing packaged war/jar file). You can set an empty value ("" or " " - without quotes) when using only console logs (without saving logs to a file) | `logs/server.log`, `C://logs/server.log`, ` ` |               |
|   **SPOTIFY_OAUTH2_CLIENT_ID**   | **Client ID** copied from the Spotify application console                                                                                                                                                                                                                                                                                                        |               `abc123abc123abc`               |               |                                                                                                                                                                                                                                                                                                       
| **SPOTIFY_OAUTH2_CLIENT_SECRET** | **Client Secret** copied from the Spotify application console                                                                                                                                                                                                                                                                                                    |               `abc123abc123abc`               |               |                                                                                                                                                                                                                                                                                                   
|       **SPOTIFY_TIMEOUT**        | Spotify server timeout (expressed in seconds)                                                                                                                                                                                                                                                                                                                    |                     `30`                      |     `15`      |

## Steps to Setup

#### 1. Configure Spotify Developer account 

Go to: https://developer.spotify.com/dashboard/. Create a free Spotify account (if you don't have one) and log in to Spotify Developer. Then create the app following the instructions on the browser screen. In the next step, add the following **Redirect URI**: `http://localhost:8080/login/oauth2/code/spotify`. From the Spotify application console, copy the **Client ID** and **Client Secret** information.

#### 2. Clone the repository

```bash
git clone https://github.com/marcinm312/springboot-vaadin-spotify.git
```

### Option 1

#### 3. Create a launch configuration in your favorite IDE

Using the example of IntelliJ IDE, select **JDK (Java) version 21**. Select the main class: `pl.marcinm312.springbootspotify.SpringBootSpotifyTrackSearchApplication` and set the environment variables as described above.

#### 4. Run the application using the configuration created in the previous step.

### Option 2

#### 3. Configure the environment variables on your OS as described above

#### 4. Package the application and then run it like so

Type the following commands from the root directory of the project:
```bash
mvn clean package
java -Dfile.encoding=UTF-8 -Djdk.util.jar.enableMultiRelease=false -jar target/springboot-spotify-track-search-0.0.1-SNAPSHOT.war
```

## API documentation
After launching the application, the API documentation is available at the URL:
http://localhost:8080/swagger-ui/index.html

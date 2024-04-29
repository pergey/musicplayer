package com.example.musicplayer5;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerController implements Initializable {

    @FXML
    private ImageView back;

    @FXML
    private ComboBox<String> cbSpeed;

    @FXML
    private Label endTime;

    @FXML
    private Label startTime;

    @FXML
    private ImageView next;

    @FXML
    private ImageView play;

    private boolean isPause = false;


    @FXML
    private ImageView previous;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Slider sVolume;

    @FXML
    private Label songAuthor;

    @FXML
    private ImageView songImage;

    @FXML
    private Label songName;

    private Media media;
    private MediaPlayer mediaPlayer;

    private File songFile;

    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};

    private Timer timer;
    private TimerTask task;
    private boolean running;

    //For changing windows
    private Stage stage;
    private Scene scene;

    static private String songId;

    static public void SongId(String id)
    {
        songId = id;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        String response = Utilities.makeHttpRequest("http://localhost/player/api/v1/songs/get/" + songId);
        Gson gson = new Gson();
        Song song = gson.fromJson(response, Song.class);


        songFile = new File("../song.mp3");
        Utilities.downloadFile("http://localhost" + song.audio,songFile);
        songName.setText(song.title);
        String highQualityImage = song.thumbnails.get(1).url.substring(0, song.thumbnails.get(1).url.indexOf('='));
        songImage.setImage(Utilities.downloadImage(highQualityImage));
        songAuthor.setText(song.artists.get(0));

        setMedia();
        playMedia();

        for(int i = 0; i < speeds.length; ++i)
        {
            cbSpeed.getItems().add(Integer.toString(speeds[i]) + '%');
        }

        Image pauseImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons/pause.png")));
        Image playImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons/play.png")));

        cbSpeed.setOnAction(this::changeSpeed);

        sVolume.valueProperty().addListener((observableValue, number, t1) -> mediaPlayer.setVolume(sVolume.getValue() * 0.01));

        play.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent)
            {
                if(!isPause)
                {
                    pauseMedia();
                   play.setImage(playImage);
                    isPause = true;
                } else {
                    playMedia();
                   play.setImage(pauseImage);
                    isPause = false;
                }

            }});

        back.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent)
            {
                try {
                    switchToMainScreen(mouseEvent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }});
    }

    public void switchToMainScreen(MouseEvent event) throws IOException
    {
        cancleTimer();
        mediaPlayer.stop();

        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void setMedia()
    {

        media = new Media(songFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
    }

    public void playMedia()
    {
        beginTimer();
        changeSpeed(null);

        mediaPlayer.setVolume(sVolume.getValue() * 0.01); //fix some bag

        mediaPlayer.play();
    }

    public void pauseMedia()
    {
        cancleTimer();
        mediaPlayer.pause();
    }

    public void resetMedia()
    {
        progressBar.setProgress(0);
        mediaPlayer.seek(Duration.seconds(0));
    }


    public void changeSpeed(ActionEvent event)
    {
        if(cbSpeed.getValue() == null)
        {
            mediaPlayer.setRate(1);
        } else {
            mediaPlayer.setRate(Integer.parseInt(cbSpeed.getValue().substring(0, cbSpeed.getValue().length() - 1)) * 0.01);
        }
    }

    public void beginTimer()
    {
        timer = new Timer();
        task = new TimerTask() {
            public void run()
            {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();

                if(media == null)
                {
                    return;
                }

                double end = media.getDuration().toSeconds();

                startTime.setText(String.valueOf(current));
                endTime.setText(String.valueOf(end));

                System.out.println(current/end);
                progressBar.setProgress(current/end);

                if(current/end == 1)
                {
                    cancleTimer();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void cancleTimer()
    {
        running = false;
        timer.cancel();
    }

}

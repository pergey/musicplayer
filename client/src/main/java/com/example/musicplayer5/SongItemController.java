package com.example.musicplayer5;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import javax.management.monitor.MonitorSettingException;
import java.net.URL;
import java.util.ResourceBundle;

public class SongItemController implements Initializable {


    @FXML
    private Label songAuthor;

    @FXML
    private ImageView songImage;

    @FXML
    private Label songName;

    @FXML
    private Label songDuration;


    @FXML
    private HBox box;

    private String songId;

    public void setSong(Song song)
    {
        songImage.setImage(Utilities.downloadImage(song.getThumbnails().get(0).url));
        songAuthor.setText(song.getArtists().get(0));
        songName.setText(song.getTitle());
        songId = song.getId();
        songDuration.setText(song.getDuration());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        box.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> setSongId(event, box));
    }

    public void setSongId(MouseEvent event, HBox box)
    {
        PlayerController.SongId(songId);
    }

}
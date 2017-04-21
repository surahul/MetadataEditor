package varunest.com.metadataeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

import android_file.io.File;
import varunest.com.metadataeditor.folderpicker.EventCallback;
import varunest.com.metadataeditor.folderpicker.FolderPickerConfig;
import varunest.com.metadataeditor.folderpicker.FolderPickerDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_PICKER = 0x12;
    // Views
    private View noContentView;
    private View infoContainer;
    private Button pickAudioButton, saveMetaButton;
    private TextView audioName, artistName, albumName, albumArtistName, year, genre;
    private ImageView albumCover;

    private AudioFile audioFile;
    private Bitmap newAlbumImage;

    private FolderPickerDialog.FolderSelectCallback folderSelectCallback = new FolderPickerDialog.FolderSelectCallback() {
        @Override
        public void onFolderSelection(File file) {
        }

        @Override
        public void onFileSelection(File file) {
            // TODO : show file meta details.
            String fileName = file.getName();
            if (GeneralUtils.determineExtension(fileName).equals("mp3") || GeneralUtils.determineExtension(fileName).equals("m4a")) {
                try {
                    audioFile = AudioFileIO.read(file.getWrappedFile());
                    infoContainer.setVisibility(View.VISIBLE);
                    noContentView.setVisibility(View.GONE);
                    loadMetaDetailsInUI();
                    saveMetaButton.setVisibility(View.VISIBLE);
                    newAlbumImage = null;
                } catch (CannotReadException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "CannotReadException", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "IOException", Toast.LENGTH_SHORT).show();
                } catch (TagException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "TagException", Toast.LENGTH_SHORT).show();
                } catch (ReadOnlyFileException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "ReadOnlyFileException", Toast.LENGTH_SHORT).show();
                } catch (InvalidAudioFrameException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "InvalidAudioFrameException", Toast.LENGTH_SHORT).show();
                }
            } else {
                noContentView.setVisibility(View.VISIBLE);
                infoContainer.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "This file type is not supported. Choose either mp3 or m4a.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wireUpWidgets();
    }


    private void wireUpWidgets() {
        noContentView = findViewById(R.id.no_content_view);
        infoContainer = findViewById(R.id.info_container);
        audioName = (TextView) findViewById(R.id.audio_name);
        artistName = (TextView) findViewById(R.id.audio_artist);
        albumName = (TextView) findViewById(R.id.audio_album);
        albumCover = (ImageView) findViewById(R.id.audio_cover);
        albumArtistName = (TextView) findViewById(R.id.audio_album_artist);
        year = (TextView) findViewById(R.id.audio_year);
        genre = (TextView) findViewById(R.id.audio_genre);
        pickAudioButton = (Button) findViewById(R.id.pick_audio_button);
        saveMetaButton = (Button) findViewById(R.id.save_meta_button);

        pickAudioButton.setOnClickListener(this);
        saveMetaButton.setOnClickListener(this);
        findViewById(R.id.album_change_button).setOnClickListener(this);
    }

    private void loadMetaDetailsInUI() {
        Tag audioTag = audioFile.getTag();
        if (audioTag.getFirst(FieldKey.TITLE) != null && !audioTag.getFirst(FieldKey.TITLE).isEmpty()) {
            audioName.setText(audioTag.getFirst(FieldKey.TITLE));
        } else {
            audioName.setHint(getString(R.string.not_available));
            audioName.setText("");
        }

        if (audioTag.getFirst(FieldKey.ARTIST) != null && !audioTag.getFirst(FieldKey.ARTIST).isEmpty()) {
            artistName.setText(audioTag.getFirst(FieldKey.ARTIST));
        } else {
            artistName.setHint(getString(R.string.not_available));
            artistName.setText("");
        }

        if (audioTag.getFirst(FieldKey.ALBUM) != null && !audioTag.getFirst(FieldKey.ALBUM).isEmpty()) {
            albumName.setText(audioTag.getFirst(FieldKey.ALBUM));
        } else {
            albumName.setHint(getString(R.string.not_available));
            albumName.setText("");
        }

        if (audioTag.getFirst(FieldKey.ALBUM_ARTIST) != null && !audioTag.getFirst(FieldKey.ALBUM_ARTIST).isEmpty()) {
            albumArtistName.setText(audioTag.getFirst(FieldKey.ALBUM_ARTIST));
        } else {
            albumArtistName.setHint(getString(R.string.not_available));
            albumArtistName.setText("");
        }

        if (audioTag.getFirst(FieldKey.YEAR) != null && !audioTag.getFirst(FieldKey.YEAR).isEmpty()) {
            year.setText(audioTag.getFirst(FieldKey.YEAR));
        } else {
            year.setHint(getString(R.string.not_available));
            year.setText("");
        }

        if (audioTag.getFirst(FieldKey.GENRE) != null && !audioTag.getFirst(FieldKey.GENRE).isEmpty()) {
            genre.setText(audioTag.getFirst(FieldKey.GENRE));
        } else {
            genre.setHint(getString(R.string.not_available));
            genre.setText("");
        }

        if (audioTag.getArtworkList() != null && audioTag.getArtworkList().size() > 0) {
            try {
                albumCover.setImageBitmap(audioTag.getArtworkList().get(0).getImage());
            } catch (IOException e) {
                albumCover.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
                e.printStackTrace();
            }
        } else {
            albumCover.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pick_audio_button:
                FolderPickerConfig folderPickerConfig = new FolderPickerConfig.Builder()
                        .setDefaultDirectory(Environment.getExternalStorageDirectory().getAbsolutePath(), new EventCallback() {
                            @Override
                            public void onEvent(Object object) {
                                Toast.makeText(MainActivity.this, "You need to give SAF permission before continuing.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .showCancelButton(true)
                        .showHiddenFiles(false)
                        .showNonDirectoryFiles(true)
                        .enableFilePickMode(true)
                        .build();
                FolderPickerDialog.newInstance(folderPickerConfig).setFolderSelectionCallback(folderSelectCallback).show(MainActivity.this);
                break;

            case R.id.save_meta_button:
                try {
                    saveMetaInfoOfCurrentSelectedAudio();
                    Toast.makeText(MainActivity.this, "Meta Info Updated Successfully.", Toast.LENGTH_LONG).show();
                } catch (FieldDataInvalidException | CannotWriteException | NumberFormatException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;

            case R.id.album_change_button:
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, REQUEST_CODE_PICKER);
                break;
        }
    }

    private void saveMetaInfoOfCurrentSelectedAudio() throws FieldDataInvalidException, CannotWriteException, IOException {
        if (audioFile != null) {
            Tag tag = audioFile.getTag();
            if (!audioName.getText().toString().isEmpty()) {
                try {
                    tag.setField(FieldKey.TITLE, audioName.getText().toString());
                } catch (FieldDataInvalidException e) {
                    throw new FieldDataInvalidException("Invalid Audio Title");
                }
            }

            if (!artistName.getText().toString().isEmpty()) {
                try {
                    tag.setField(FieldKey.ARTIST, artistName.getText().toString());
                } catch (FieldDataInvalidException e) {
                    throw new FieldDataInvalidException("Invalid Artist");
                }
            }


            if (!albumName.getText().toString().isEmpty()) {
                try {
                    tag.setField(FieldKey.ALBUM, albumName.getText().toString());
                } catch (FieldDataInvalidException e) {
                    throw new FieldDataInvalidException("Invalid Album");
                }
            }

            if (!albumArtistName.getText().toString().isEmpty()) {
                try {
                    tag.setField(FieldKey.ALBUM_ARTIST, albumArtistName.getText().toString());
                } catch (FieldDataInvalidException e) {
                    throw new FieldDataInvalidException("Invalid Album Artist");
                }
            }

            if (!genre.getText().toString().isEmpty()) {
                try {
                    tag.setField(FieldKey.GENRE, genre.getText().toString());
                } catch (FieldDataInvalidException e) {
                    throw new FieldDataInvalidException("Invalid Genre");
                }
            }

            if (!year.getText().toString().isEmpty()) {
                try {
                    tag.setField(FieldKey.YEAR, String.valueOf(Integer.parseInt(year.getText().toString())));
                } catch (FieldDataInvalidException e) {
                    throw new FieldDataInvalidException("Invalid Year");
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid Year");
                }
            }

            if (newAlbumImage != null) {
                Artwork artwork = ArtworkFactory.createArtworkFromBitmap(newAlbumImage);
                tag.deleteArtworkField();
                tag.setField(artwork);
            }

            audioFile.commit();
        } else {
            throw new IllegalStateException("AudioFile cannot be null while performing meta update operation");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            try {
                //Decode image size
                BitmapFactory.Options optionsIn = new BitmapFactory.Options();
                optionsIn.inJustDecodeBounds = true; // the trick is HERE, avoiding memory leaks
                BitmapFactory.decodeStream(this.getContentResolver().openInputStream(data.getData()), null, optionsIn);

                BitmapFactory.Options optionsOut = new BitmapFactory.Options();
                int requiredWidth = 500;
                float bitmapWidth = optionsIn.outWidth;
                int scale = 1;
                if (requiredWidth <= bitmapWidth) {
                    scale = Math.round(bitmapWidth / requiredWidth);
                }
                optionsOut.inSampleSize = scale;
                optionsOut.inPurgeable = true;//avoiding memory leaks
                newAlbumImage = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(data.getData()), null, optionsOut);
                albumCover.setImageBitmap(newAlbumImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

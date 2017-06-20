package com.fivevsthree.puzzlecube.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Controllers.ScreenController;
import com.fivevsthree.puzzlecube.Models.Puzzle;

public class OptionsScreen implements Screen {

	private PuzzleCube game;

	private Stage stage;
	private Skin skin;

	private ScreenController screenController;
	private InputMultiplexer inputMultiplexer;

	private TextButton backButton, soundButton, musicButton, timerButton,
			counterButton, faceButton, defaultsButton, rotationButton,
			solvedButton;

	private Slider soundSlider, musicSlider, touchSlider, redSlider,
			greenSlider, blueSlider;

	private Label touchLabel, redLabel, greenLabel, blueLabel, rotationLabel,
			solvedLabel;

	private ScrollPane scrollPane;

	private Image colorSample;

	private Music testMusic;
	private Sound testSound;

	private int currentFace, currentRotation, currentSolved;

	private float[] colors;

	private String[] faces = { "Top", "Bottom", "Right", "Left", "Front",
			"Back" };

	private int[] faceOrder = { 1, 0, 5, 4, 3, 2 };

	private Preferences preferences;

	public OptionsScreen(PuzzleCube game) {
		this.game = game;

		// Set up the stage
		stage = new Stage(800, 480, true);

		// Get skin asset
		skin = game.getAssets().get("data/skins/ui.json");

		if (game.getAssets().isLoaded(PuzzleCube.PUZZLE_MUSIC_FILE)) {
			testMusic = game.getAssets().get(PuzzleCube.PUZZLE_MUSIC_FILE);
		}

		if (game.getAssets().isLoaded(PuzzleCube.INVALID_SOUND_FILE)) {
			testSound = game.getAssets().get(PuzzleCube.INVALID_SOUND_FILE);
		}

		// Set texture filter for font otherwise it will look bad
		skin.getFont("default").getRegion().getTexture();

		preferences = Gdx.app.getPreferences(PuzzleCube.PREFERENCES_FILE);

		createUI();
	}

	/**
	 * Set up UI elements
	 */
	private void createUI() {
		backButton = new TextButton("Back to Menu", skin);
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showMainMenu();
			}
		});

		defaultsButton = new TextButton("Use Defaults", skin);
		defaultsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				soundButton.setChecked(true);
				soundSlider.setValue(PuzzleCube.DEFAULT_SOUND_VOLUME);
				musicButton.setChecked(true);
				musicSlider.setValue(PuzzleCube.DEFAULT_MUSIC_VOLUME);
				timerButton.setChecked(true);
				counterButton.setChecked(true);
				touchSlider.setValue(PuzzleCube.DEFAULT_TOUCH_SENSITIVITY);

				currentRotation = 0;
				rotationButton.setText(Puzzle.RotationAnimation.values()[currentRotation]
						.name());

				currentSolved = 0;
				solvedButton.setText(Puzzle.SolvedAnimation.values()[currentSolved]
						.name());

				for (int i = 0, j; i < 6; i++) {
					j = faceOrder[i];
					colors[j * 3] = PuzzleCube.DEFAULT_COLOR[j].r;
					colors[j * 3 + 1] = PuzzleCube.DEFAULT_COLOR[j].g;
					colors[j * 3 + 2] = PuzzleCube.DEFAULT_COLOR[j].b;
				}

				currentFace = faces.length - 1;
				faceButton.fire(new ChangeEvent());
			}
		});

		soundButton = new TextButton("Sound: Off", skin, "toggle");
		soundButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton button = (TextButton) actor;
				if (button.isChecked()) {
					button.setText("Sound: On");
					soundSlider.setTouchable(Touchable.enabled);
					skin.setEnabled(soundSlider, true);
				} else {
					button.setText("Sound: Off");
					soundSlider.setTouchable(Touchable.disabled);
					skin.setEnabled(soundSlider, false);
				}
			}
		});

		musicButton = new TextButton("Music: Off", skin, "toggle");
		musicButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton button = (TextButton) actor;
				if (button.isChecked()) {
					button.setText("Music: On");
					musicSlider.setTouchable(Touchable.enabled);
					skin.setEnabled(musicSlider, true);
				} else {
					button.setText("Music: Off");
					musicSlider.setTouchable(Touchable.disabled);
					skin.setEnabled(musicSlider, false);
				}
			}
		});

		timerButton = new TextButton("Timer: Hide", skin, "toggle");
		timerButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton button = (TextButton) actor;
				if (button.isChecked()) {
					button.setText("Timer: Show");
				} else {
					button.setText("Timer: Hide");
				}
			}
		});

		counterButton = new TextButton("Counter: Hide", skin, "toggle");
		counterButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton button = (TextButton) actor;
				if (button.isChecked()) {
					button.setText("Counter: Show");
				} else {
					button.setText("Counter: Hide");
				}
			}
		});

		faceButton = new TextButton("Face: Top", skin);
		faceButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton button = (TextButton) actor;
				if (++currentFace > faces.length - 1) {
					currentFace = 0;
				}
				button.setText(String.format("Face: %s", faces[currentFace]));

				int j = faceOrder[currentFace];
				redSlider.setValue(colors[j * 3]);
				greenSlider.setValue(colors[j * 3 + 1]);
				blueSlider.setValue(colors[j * 3 + 2]);
			}
		});

		rotationButton = new TextButton("Bounce", skin);
		rotationButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton button = (TextButton) actor;
				if (++currentRotation > Puzzle.RotationAnimation.values().length - 1) {
					currentRotation = 0;
				}
				button.setText(Puzzle.RotationAnimation.values()[currentRotation]
						.name());
			}
		});

		solvedButton = new TextButton("NoAnimation", skin);
		solvedButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				TextButton button = (TextButton) actor;
				if (++currentSolved > Puzzle.SolvedAnimation.values().length - 1) {
					currentSolved = 0;
				}
				button.setText(Puzzle.SolvedAnimation.values()[currentSolved]
						.name());
			}
		});

		InputListener stopTouchDown = new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				event.stop();
				return true;
			}
		};

		soundSlider = new Slider(0f, 1f, 0.1f, false, skin);
		soundSlider.setAnimateDuration(0.1f);
		soundSlider.setTouchable(Touchable.disabled);
		soundSlider.addListener(stopTouchDown);
		soundSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Slider slider = (Slider) actor;
				if (testSound != null && slider.isDragging()) {
					testSound.play(slider.getValue());
				}
			}
		});
		skin.setEnabled(soundSlider, false);

		musicSlider = new Slider(0f, 1f, 0.1f, false, skin);
		musicSlider.setAnimateDuration(0.1f);
		musicSlider.setTouchable(Touchable.disabled);
		musicSlider.addListener(stopTouchDown);
		musicSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Slider slider = (Slider) actor;
				if (testMusic != null) {
					testMusic.setVolume(slider.getValue());
					if (!testMusic.isPlaying() && slider.isDragging()) {
						testMusic.play();
					}
				}
			}
		});
		musicSlider.addListener(new InputListener() {
			@Override
			public void exit(InputEvent event, float x, float y, int pointer,
					Actor toActor) {
				if (testMusic != null) {
					testMusic.stop();
				}
				super.exit(event, x, y, pointer, toActor);
			}
		});
		skin.setEnabled(musicSlider, false);

		touchSlider = new Slider(5f, 15f, 1f, false, skin);
		touchSlider.setAnimateDuration(0.1f);
		touchSlider.addListener(stopTouchDown);

		touchLabel = new Label("Sensitivity", skin);
		touchLabel.setAlignment(Align.center);
		touchLabel.setHeight(backButton.getHeight());

		colorSample = new Image(skin, "color-sample");

		redLabel = new Label("Red", skin);
		redLabel.setAlignment(Align.center);

		greenLabel = new Label("Green", skin);
		greenLabel.setAlignment(Align.center);

		blueLabel = new Label("Blue", skin);
		blueLabel.setAlignment(Align.center);

		rotationLabel = new Label("Layer Rotation", skin);
		rotationLabel.setAlignment(Align.center);

		solvedLabel = new Label("Solved Puzzle", skin);
		solvedLabel.setAlignment(Align.center);

		ClickListener updateColorArray = new ClickListener() {
			@Override
			public void exit(InputEvent event, float x, float y, int pointer,
					Actor toActor) {
				int j = faceOrder[currentFace];
				colors[j * 3] = redSlider.getValue();
				colors[j * 3 + 1] = greenSlider.getValue();
				colors[j * 3 + 2] = blueSlider.getValue();
			}

		};

		ChangeListener updateSampleColor = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				colorSample.setColor(redSlider.getValue(),
						greenSlider.getValue(), blueSlider.getValue(), 1f);
			}
		};

		redSlider = new Slider(0f, 1f, 0.01f, false, skin);
		redSlider.setAnimateDuration(0.1f);
		redSlider.addListener(stopTouchDown);
		redSlider.addListener(updateSampleColor);
		redSlider.addListener(updateColorArray);

		greenSlider = new Slider(0f, 1f, 0.01f, false, skin);
		greenSlider.setAnimateDuration(0.1f);
		greenSlider.addListener(stopTouchDown);
		greenSlider.addListener(updateSampleColor);
		greenSlider.addListener(updateColorArray);

		blueSlider = new Slider(0f, 1f, 0.01f, false, skin);
		blueSlider.setAnimateDuration(0.1f);
		blueSlider.addListener(stopTouchDown);
		blueSlider.addListener(updateSampleColor);
		blueSlider.addListener(updateColorArray);

		scrollPane = new ScrollPane(null);
		scrollPane.setFillParent(true);
		scrollPane.setTransform(false);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Update actors on the stage
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		stage.clear();

		soundButton.setChecked(preferences.getBoolean("sound", true));
		soundSlider.setValue(preferences.getFloat("sound-volume",
				PuzzleCube.DEFAULT_SOUND_VOLUME));
		musicButton.setChecked(preferences.getBoolean("music", true));
		musicSlider.setValue(preferences.getFloat("music-volume",
				PuzzleCube.DEFAULT_MUSIC_VOLUME));
		timerButton.setChecked(preferences.getBoolean("timer", true));
		counterButton.setChecked(preferences.getBoolean("counter", true));
		touchSlider.setValue(preferences.getFloat("touch",
				PuzzleCube.DEFAULT_TOUCH_SENSITIVITY));

		currentRotation = preferences.getInteger("rotation", 0);
		rotationButton
				.setText(Puzzle.RotationAnimation.values()[currentRotation]
						.name());

		currentSolved = preferences.getInteger("solved", 0);
		solvedButton.setText(Puzzle.SolvedAnimation.values()[currentSolved]
				.name());

		colors = new float[18];
		for (int i = 0, j; i < 6; i++) {
			j = faceOrder[i];
			colors[j * 3] = preferences.getFloat(
					String.format("color-%d-r", j),
					PuzzleCube.DEFAULT_COLOR[j].r);
			colors[j * 3 + 1] = preferences.getFloat(
					String.format("color-%d-g", j),
					PuzzleCube.DEFAULT_COLOR[j].g);
			colors[j * 3 + 2] = preferences.getFloat(
					String.format("color-%d-b", j),
					PuzzleCube.DEFAULT_COLOR[j].b);
		}

		currentFace = faces.length - 1;
		faceButton.fire(new ChangeEvent());

		// Create our table of option controls
		Table table = new Table(skin);
		table.setTransform(false);

		float cellWidth = stage.getWidth() / 2;
		float rowHeight = 85f;

		table.row().fill();
		table.add(backButton).width(cellWidth);
		table.add(defaultsButton).width(cellWidth);

		table.row().fill();
		table.add(soundButton).width(cellWidth);
		table.add(soundSlider).expandX().pad(0, 10, 0, 10);

		table.row().fill();
		table.add(musicButton).width(cellWidth);
		table.add(musicSlider).expandX().pad(0, 10, 0, 10);

		table.row().fill();
		table.add(timerButton).width(cellWidth);
		table.add(counterButton).width(cellWidth);

		table.row().fill().center().height(rowHeight);
		table.add(rotationLabel).width(cellWidth);
		table.add(rotationButton).width(cellWidth);

		table.row().fill().center().height(rowHeight);
		table.add(solvedLabel).width(cellWidth);
		table.add(solvedButton).width(cellWidth);

		table.row().fill().center().height(rowHeight);
		table.add(touchLabel).width(cellWidth);
		table.add(touchSlider).expandX().pad(0, 10, 0, 10);

		table.row().fill();
		table.add(faceButton).width(cellWidth);
		table.add(colorSample).expand().pad(10);

		table.row().fill().center().height(rowHeight);
		table.add(redLabel).width(cellWidth);
		table.add(redSlider).expandX().pad(0, 10, 0, 10);

		table.row().fill().center().height(rowHeight);
		table.add(greenLabel).width(cellWidth);
		table.add(greenSlider).expandX().pad(0, 10, 0, 10);

		table.row().fill().center().height(rowHeight);
		table.add(blueLabel).width(cellWidth);
		table.add(blueSlider).expandX().pad(0, 10, 0, 10);

		scrollPane.setWidget(table);
		scrollPane.pack();

		stage.addActor(scrollPane);

		screenController = new ScreenController(game);
		inputMultiplexer = new InputMultiplexer(screenController, stage);

		// Process input from our stage
		Gdx.input.setInputProcessor(inputMultiplexer);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
	}

	private void savePreferences() {
		preferences.putBoolean("sound", soundButton.isChecked());
		preferences.putFloat("sound-volume", soundSlider.getValue());
		preferences.putBoolean("music", musicButton.isChecked());
		preferences.putFloat("music-volume", musicSlider.getValue());
		preferences.putBoolean("timer", timerButton.isChecked());
		preferences.putBoolean("counter", counterButton.isChecked());
		preferences.putFloat("touch", touchSlider.getValue());
		preferences.putInteger("rotation", currentRotation);
		preferences.putInteger("solved", currentSolved);

		for (int i = 0, j; i < 6; i++) {
			j = faceOrder[i];
			preferences.putFloat(String.format("color-%d-r", j), colors[j * 3]);
			preferences.putFloat(String.format("color-%d-g", j),
					colors[j * 3 + 1]);
			preferences.putFloat(String.format("color-%d-b", j),
					colors[j * 3 + 2]);
		}

		preferences.flush();
	}

	@Override
	public void hide() {
		savePreferences();
	}

	@Override
	public void pause() {
		savePreferences();
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		if (stage != null) {
			stage.dispose();
			stage = null;
		}
	}

}

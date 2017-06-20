package com.fivevsthree.puzzlecube.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Controllers.ScreenController;

public class CreditsScreen implements Screen {

	private PuzzleCube game;

	private Stage stage;
	private Skin skin;

	private ScreenController screenController;
	private InputMultiplexer inputMultiplexer;

	private ScrollPane scrollPane;

	private TextButton backButton;

	public CreditsScreen(PuzzleCube game) {
		this.game = game;

		// Set up the stage
		stage = new Stage(800, 480, true);

		// Get skin asset
		skin = game.getAssets().get("data/skins/ui.json");

		createUI();

		screenController = new ScreenController(game);
		inputMultiplexer = new InputMultiplexer(screenController, stage);
	}

	private void createUI() {
		backButton = new TextButton("Back to Menu", skin);
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showMainMenu();
			}
		});

		scrollPane = new ScrollPane(null);
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
		Table table = new Table();
		table.setFillParent(true);
		table.setTransform(false);

		float cellWidth = stage.getWidth() / 2;

		table.row();
		table.add(backButton).width(cellWidth);
		table.add().width(cellWidth);
		
		table.row().expand();

		stage.clear();
		stage.addActor(table);

		// Process input from our stage
		Gdx.input.setInputProcessor(inputMultiplexer);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
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

package com.fivevsthree.puzzlecube.Views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Models.Cube;
import com.fivevsthree.puzzlecube.Models.Puzzle;

/**
 * Render the puzzle mesh
 * 
 * @author splude@fivevsthree.com
 * 
 */
public class PuzzleRenderer {

	private ShaderProgram shader;
	private Texture texture;

	/**
	 * Initialize the renderer
	 */
	public PuzzleRenderer(PuzzleCube game) {
		shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/cube.vert"),
				Gdx.files.internal("data/shaders/cube.frag"));

		// Get the texture to apply to the cubes
		texture = game.getAssets().get("data/textures/mask.png");
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	/**
	 * Draw the puzzle and update the camera
	 * 
	 * @param puzzle
	 *            puzzle to draw
	 * @param delta
	 *            time in seconds since last render
	 */
	public void render(Puzzle puzzle, float delta) {
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

		// This will make sure cubes closest to camera will be drawn in front
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		if (puzzle != null) {
			puzzle.update(delta);

			// Make sure there were no problems compiling the shader
			if (shader.isCompiled()) {
				shader.begin();
				shader.setUniformMatrix("u_camera", puzzle.getCamera()
						.getCombinedView());
				shader.setUniformf("u_color", puzzle.color);

				texture.bind();

				/*
				 * We do not need to set the uniform for the texture. It appears
				 * that is done automatically. Setting it will cause an
				 * exception on an Android device.
				 */

				for (Cube cube : puzzle.getCubes()) {
					/*
					 * We keep a model for each logical cube so we can apply
					 * transformations to each individual cube.
					 */
					shader.setUniformMatrix("u_rotation", cube.rotation);

					/*
					 * Render the portion of the mesh that belongs to the
					 * current logical cube.
					 */
					puzzle.getMesh().render(shader, cube.index);
				}

				shader.end();
			}
		}

		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}

	/**
	 * Clean up
	 */
	public void dispose() {
		if (shader != null) {
			shader.dispose();
			shader = null;
		}
	}
}

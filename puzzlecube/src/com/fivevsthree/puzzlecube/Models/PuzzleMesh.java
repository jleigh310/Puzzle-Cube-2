package com.fivevsthree.puzzlecube.Models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * A static mesh created from multiple cubes for fast rendering
 * 
 * @author splude@fivevsthree.com
 * 
 */
public class PuzzleMesh {

	private float scale;

	private Mesh mesh;

	// Copy of vertex positions for ray picking
	private float[] rayPickVertices = new float[1872];
	// Copy of indices for ray picking
	private short[] rayPickIndices = new short[936];

	/**
	 * Create a mesh with the provided colors and scale
	 * 
	 * @param colors
	 *            array of 6 color packed floats for each puzzle face
	 * @param scale
	 *            size of each cube
	 */
	public PuzzleMesh(float[] colors, float scale) {
		this.scale = scale;

		mesh = new Mesh(true, 624, 936, new VertexAttribute(Usage.Position, 3,
				ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
				Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				new VertexAttribute(Usage.TextureCoordinates, 2,
						ShaderProgram.TEXCOORD_ATTRIBUTE), new VertexAttribute(
						Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));

		// 4 vertices * 9 components * 6 faces
		int size = 216;

		float[] vertices = new float[26 * size];

		// x = -1 : left, x = 0 : center, x = 1 : right
		for (int i = 0, x = -1; x < 2; x++) {
			// y = -1 : bottom, y = 0 : center, y = 1 : top
			for (int y = -1; y < 2; y++) {
				// z = -1 : back, z = 0 center, z = 1 : front
				for (int z = -1; z < 2; z++) {
					// 0, 0, 0 is center cube which is not created
					if (x == 0 && y == 0 && z == 0) {
						// If we are at the center then skip to the next cube
						continue;
					}

					// Calculate vertices for current cube
					float[] v = calculateVertices(x, y, z);

					// Calculate texture coordinates for current cube
					float[] t = calculateTexCoords(x, y, z);

					float[] c = calculateColors(colors, x, y, z);

					// Get normals for current cube
					float[] n = getNormals();

					// Calculate indices for current cube
					short[] d = calculateIndices(i);

					int vIndex = 0;
					int nIndex = 0;
					int tIndex = 0;
					int cIndex = 0;

					// Loop through each vertex of the cube
					for (int j = 0, k = 0; j < size;) {
						// Add coordinates for current vertex
						rayPickVertices[i * 72 + k++] = v[vIndex];
						vertices[i * size + j++] = v[vIndex++];

						rayPickVertices[i * 72 + k++] = v[vIndex];
						vertices[i * size + j++] = v[vIndex++];

						rayPickVertices[i * 72 + k++] = v[vIndex];
						vertices[i * size + j++] = v[vIndex++];

						// Add normals for current vertex
						vertices[i * size + j++] = n[nIndex++];
						vertices[i * size + j++] = n[nIndex++];
						vertices[i * size + j++] = n[nIndex++];

						// Add texture coordinates for current vertex
						vertices[i * size + j++] = t[tIndex++];
						vertices[i * size + j++] = t[tIndex++];

						// Add colors for current vertex
						vertices[i * size + j++] = c[cIndex++];
					}

					for (int j = 0; j < 36; j++) {
						// Add indices for current cube
						rayPickIndices[i * 36 + j] = d[j];
					}

					// Advance to next cube
					i++;
				}
			}
		}

		mesh.setVertices(vertices);
		mesh.setIndices(rayPickIndices);
	}

	/**
	 * Render a cube
	 * 
	 * @param cubeIndex
	 *            index of the cube in the mesh
	 */
	public void render(ShaderProgram shader, int cubeIndex) {
		if (shader != null && shader.isCompiled()) {
			// There are 36 vertices per cube
			mesh.render(shader, GL20.GL_TRIANGLES, cubeIndex * 36, 36);
		}
	}

	/**
	 * Determine the logical cube hit based off the position of the ray
	 * intersection with the mesh. Because the mesh is static, this hit test
	 * will not work with scaled or translated cubes
	 * 
	 * @param ray
	 *            ray from the camera
	 * @param location
	 *            returns the logical location of the cube
	 * @param axis
	 *            this is used to determine the face of the cube that was hit
	 * @return was a cube was hit
	 */
	public boolean getPickRayHit(Ray ray, float[] location, float[] axis) {
		Vector3 intersection = new Vector3();

		// Was a cube hit and which cube was hit
		boolean isHit = Intersector.intersectRayTriangles(ray, rayPickVertices,
				rayPickIndices, 3, intersection);

		if (isHit) {
			// Calculate the logical cube location from the mesh position
			location[0] = Math.round(Math.min(
					Math.max(intersection.x / scale, -2.9), 2.9) / 2);
			location[1] = Math.round(Math.min(
					Math.max(intersection.y / scale, -2.9), 2.9) / 2);
			location[2] = Math.round(Math.min(
					Math.max(intersection.z / scale, -2.9), 2.9) / 2);

			// We only want positive values
			float x = Math.abs(intersection.x);
			float y = Math.abs(intersection.y);
			float z = Math.abs(intersection.z);

			/*
			 * Determine the face of the cube that was hit. Since no more than
			 * one face on the same axis will be visible at a time, we don't
			 * care exactly which face it is.
			 */

			// The right or left face was hit
			axis[0] = ((x > y && x > z) ? 1 : 0)
					* ((intersection.x > 0) ? 1 : -1);

			// The top or bottom face was hit
			axis[1] = ((y > x && y > z) ? 1 : 0)
					* ((intersection.y > 0) ? 1 : -1);

			// The front or back face was hit
			axis[2] = ((z > y && z > x) ? 1 : 0)
					* ((intersection.z > 0) ? 1 : -1);
		}

		return isHit;
	}

	/**
	 * Calculate the vertices for a cube a the given location
	 * 
	 * @param x
	 *            x position relative to center of puzzle
	 * @param y
	 *            y position relative to center of puzzle
	 * @param z
	 *            z position relative to center of puzzle
	 * @return array of vertices
	 */
	private float[] calculateVertices(float x, float y, float z) {
		// Move coordinates to fit cubes according to their size
		x *= scale * 2f;
		y *= scale * 2f;
		z *= scale * 2f;

		// @formatter:off

		// 0|      |3
		//  |      |
		// 1|______|2
	
		float[] vertices = {
				// Bottom
				x + scale, y - scale, z - scale,
				x + scale, y - scale, z + scale,
				x - scale, y - scale, z + scale,
				x - scale, y - scale, z - scale,

				// Top
				x - scale, y + scale, z - scale,
				x - scale, y + scale, z + scale,
				x + scale, y + scale, z + scale,
				x + scale, y + scale, z - scale,

				// Back
				x + scale, y + scale, z - scale,
				x + scale, y - scale, z - scale,
				x - scale, y - scale, z - scale,
				x - scale, y + scale, z - scale,

				// Front
				x - scale, y + scale, z + scale,
				x - scale, y - scale, z + scale,
				x + scale, y - scale, z + scale,
				x + scale, y + scale, z + scale,

				// Left
				x - scale, y + scale, z - scale,
				x - scale, y - scale, z - scale,
				x - scale, y - scale, z + scale,
				x - scale, y + scale, z + scale,


				// Right
				x + scale, y + scale, z + scale,
				x + scale, y - scale, z + scale,
				x + scale, y - scale, z - scale,
				x + scale, y + scale, z - scale, };
		
		// @formatter:on

		return vertices;
	}

	/**
	 * Return an array of texture coordinates for each face of a cube
	 * 
	 * @param x
	 *            x position relative to center of puzzle
	 * @param y
	 *            y position relative to center of puzzle
	 * @param z
	 *            z position relative to center of puzzle
	 * @return array of texture coordinates
	 */
	private float[] calculateTexCoords(int x, int y, int z) {
		// @formatter:off
			
		// Texture coordinates are not affected by index order, but vertex
			
		// order does matter
		// 0,0|      |1,0
		//    |      |
		// 0,1|______|1,1
			
		// @formatter:on

		return new float[] {
				// Bottom
				0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,

				// Top
				0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,

				// Back
				0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,

				// Front
				0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,

				// Left
				0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,

				// Right
				0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f };
	}

	/**
	 * Return an array of colors for each face of a cube
	 * 
	 * @param colors
	 *            float array of colors( r, g, b, a for 6 puzzle faces)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private float[] calculateColors(float[] colors, int x, int y, int z) {
		float[] faces = new float[24];

		// Loop through all faces of a cube
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 4; j++) {
				// If the face belongs to a face of the puzzle then color it
				if ((i == 0 && y == -1) || (i == 1 && y == 1)
						|| (i == 2 && z == -1) || (i == 3 && z == 1)
						|| (i == 4 && x == -1) || (i == 5 && x == 1)) {
					faces[i * 4 + j] = Color.toFloatBits(colors[i * 3],
							colors[i * 3 + 1], colors[i * 3 + 2], 1f);
				} else {
					// The face does not belong to a puzzle face so make it dark
					faces[i * 4 + j] = Color.toFloatBits(38, 38, 38, 255);
				}
			}
		}

		return faces;
	}

	/**
	 * Get normals for each face of the cube
	 * 
	 * @return array of normals
	 */
	private float[] getNormals() {
		// @formatter:off
		
		float[] normals = {
				// Bottom
				 0f, -1f,  0f,
				 0f, -1f,  0f,
				 0f, -1f,  0f,
				 0f, -1f,  0f,

				// Top
				 0f,  1f,  0f,
				 0f,  1f,  0f,
				 0f,  1f,  0f,
				 0f,  1f,  0f,

				// Back
				 0f,  0f, -1f,
				 0f,  0f, -1f,
				 0f,  0f, -1f,
				 0f,  0f, -1f,

				// Front
				 0f,  0f,  1f,
				 0f,  0f,  1f,
				 0f,  0f,  1f,
				 0f,  0f,  1f,

				// Left
				-1f,  0f,  0f,
				-1f,  0f,  0f,
				-1f,  0f,  0f,
				-1f,  0f,  0f,

				// Right
				 1f,  0f,  0f,
				 1f,  0f,  0f,
				 1f,  0f,  0f,
				 1f,  0f,  0f, };
		
		// @formatter:on

		return normals;
	}

	/**
	 * Calculate indices for each cube. Indices use vertices to determine the
	 * shape of the cube
	 * 
	 * @param cubeIndex
	 *            offset of cube in the mesh
	 * @return array of indices
	 */
	private short[] calculateIndices(int cubeIndex) {
		// There are 24 indices per cube
		int i = cubeIndex * 24;

		// @formatter:off
		
		// Face made up of two triangles, each made up of 3 indices, counter clockwise
		
		// 0|       0\  |3 
		//  |         \ |
		// 1|____2     \|2

		short[] indices = {
				// Bottom
				(short) (0 + i), (short) (1 + i), (short) (2 + i),
				(short) (0 + i), (short) (2 + i), (short) (3 + i),

				// Top
				(short) (4 + i), (short) (5 + i), (short) (6 + i),
				(short) (4 + i), (short) (6 + i), (short) (7 + i),

				// Back
				(short) (8 + i), (short) (9 + i), (short) (10 + i),
				(short) (8 + i), (short) (10 + i), (short) (11 + i),

				// Front
				(short) (12 + i), (short) (13 + i), (short) (14 + i),
				(short) (12 + i), (short) (14 + i), (short) (15 + i),

				// Left
				(short) (16 + i), (short) (17 + i), (short) (18 + i),
				(short) (16 + i), (short) (18 + i), (short) (19 + i),

				// Right
				(short) (20 + i), (short) (21 + i), (short) (22 + i),
				(short) (20 + i), (short) (22 + i), (short) (23 + i), };

		// @formatter:on

		return indices;
	}

	/**
	 * Clean up
	 */
	public void dispose() {
		if (mesh != null) {
			mesh.dispose();
			mesh = null;
		}
	}
}

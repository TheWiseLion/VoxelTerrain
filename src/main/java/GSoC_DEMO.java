/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Map;

import voxelsystem.densityvolumes.shapes.BoxVolume;
import voxelsystem.densityvolumes.shapes.NoiseShape;
import voxelsystem.densityvolumes.shapes.SphereVolume;
import voxelsystem.operators.CSGOperators;
import voxelsystem.voxeldata.VoxelDensityExtractor;
import voxelsystem.voxeldata.VoxelExtractor;
import voxelsystem.voxelmaterials.VoxelType;
import voxelsystem.voxelobjects.PagingVoxelObject;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;

public class GSoC_DEMO extends SimpleApplication {

	public static void main(final String[] args) {
		final GSoC_DEMO app = new GSoC_DEMO();
		app.start();
	}

	BitmapText				position;					// (print camera position)
	BitmapText				direction;					// (print camera direction)
	BitmapText				selectedMaterialN;			// print selected material
	BitmapText				editorMode;

	private float			counter				= 0;

	Input					input;
	public VoxelEditor		editor;
	private BulletAppState	bulletAppState;
	int						selectedMaterial	= 0;
	private Geometry		cube;
	PagingVoxelObject		world;

	int						showMesh			= -1;

	@Override
	public void simpleInitApp() {
		final Map<Integer, VoxelType> materialTypes = Materials.getMaterials(this.assetManager);

		// Make a box:
		final Vector3f boxCenter = new Vector3f(0, -1, 0);// 1.24234f,2.2342f,-1.4234f
		final BoxVolume box = new BoxVolume(boxCenter, 5, 5.25f, 5);

		// Make a sphere
		final SphereVolume ss = new SphereVolume(new Vector3f(0, 0, 0), 4f);
		// Set type to rock
		ss.setSimpleType(2);
		// Set type to sand
		box.setSimpleType(2);

		// Boiler plate.....
		final SphereVolume sv2 = new SphereVolume(new Vector3f(2f, 0, 0), 4f);
		sv2.setSimpleType(1);

		final SphereVolume sv = new SphereVolume(new Vector3f(2f, 2f, 2), 5f);
		sv.setSimpleType(1);
		final float res = .5f;

		this.world = new PagingVoxelObject(res, materialTypes, new Vector3f(50, 50, 50));
		final VoxelExtractor ve = new VoxelDensityExtractor(new NoiseShape(res));

		//
		this.world.preformOperation(CSGOperators.unionOverwrite, ve.getBoundingBox(), ve);
		this.world.update(this, this.rootNode, 2500);

		this.editor = new VoxelEditor(this.world, this.rootNode, this.assetManager);

		this.flyCam.setZoomSpeed(0);
		this.flyCam.setMoveSpeed(10);
		this.cam.setLocation(new Vector3f(14.81f, 2.7f, 14.4f));

		// PHYSICS SETUP//
		this.bulletAppState = new BulletAppState();
		this.stateManager.attach(this.bulletAppState);
		// Basic world bounds
		this.bulletAppState.setWorldMin(new Vector3f(1000, 1000, 1000));
		this.bulletAppState.setWorldMin(new Vector3f(-1000, -1000, -1000));

		this.initMaterial();

		// SkyBox
		this.rootNode.attachChild(SkyFactory.createSky(this.assetManager, "Textures/BrightSky.dds", false));

		// ////CROSS HAIRS (aka + sign)///////
		this.guiFont = this.assetManager.loadFont("Interface/Fonts/Default.fnt");
		final BitmapText ch = new BitmapText(this.guiFont, false);
		ch.setSize(this.guiFont.getCharSet().getRenderedSize() * 2);
		ch.setText("+"); // crosshairs
		ch.setLocalTranslation( // center
				this.settings.getWidth() / 2 - ch.getLineWidth() / 2, this.settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
		this.guiNode.attachChild(ch);
		// ////////////////////////////

		final ScreenshotAppState screenShotState = new ScreenshotAppState();
		this.stateManager.attach(screenShotState);
	}

	@Override
	public void simpleUpdate(final float tpf) {
		// Print Camera location
		this.position.setText("" + this.cam.getLocation());
		this.direction.setText("" + this.cam.getDirection());
		this.editorMode.setText("Edit Mode: " + this.editor.getModeName());
		this.selectedMaterialN.setText("Selected Material: " + this.selectedMaterial);
		this.counter += tpf;
		this.selectedMaterial = this.selectedMaterial % 6;

		this.world.update(this, this.rootNode, 10);

		int mod = 0;
		if (this.input.addDown) {
			mod = 1;

		} else if (this.input.removeDown) {
			mod = 2;

		}

		if (this.editor.mode != 4) {
			this.input.addDown = false;
			this.input.removeDown = false;
		}

		this.editor.update(this.cam.getLocation(), this.cam.getDirection(), mod, this.selectedMaterial);
	}

	// Initialize Materials//
	public void initMaterial() {
		// Intialize Lights
		final DirectionalLight sun2 = new DirectionalLight();
		sun2.setColor(ColorRGBA.White);
		sun2.setDirection(new Vector3f(0.408248f, 0.408248f, 0.816497f).normalizeLocal().negate());
		this.rootNode.addLight(sun2);

		/* Drop shadows */
		final int SHADOWMAP_SIZE = 1024;
		final DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(this.assetManager, SHADOWMAP_SIZE, 3);
		dlsr.setLight(sun2);
		dlsr.setShadowIntensity(.25f);
		this.viewPort.addProcessor(dlsr);
		dlsr.setEnabledStabilization(true);
		dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
		dlsr.setEdgesThickness(2);
		dlsr.setLambda(1.03f);

		FilterPostProcessor fpp;
		fpp = new FilterPostProcessor(this.assetManager);

		final LightScatteringFilter filter = new LightScatteringFilter(sun2.getDirection().mult(-3000));
		filter.setEnabled(true);
		filter.setLightDensity(.25f);
		// filter.setLightDensity(f);
		// LightScatteringUI ui = new LightScatteringUI(inputManager, filter);
		fpp.addFilter(filter);
		//

		WaterFilter water;
		final float initialWaterHeight = -2f;
		water = new WaterFilter(this.rootNode, sun2.getDirection());
		water.setWaterHeight(initialWaterHeight);
		fpp.addFilter(water);
		this.viewPort.addProcessor(fpp);

		// FogFilter fog=new FogFilter();
		// fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
		// fog.setFogDistance(20);
		// fog.setFogDensity(1f);
		// fpp.addFilter(fog);
		// viewPort.addProcessor(fpp);

		final AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.55f));
		this.rootNode.addLight(al);

		// Initialize Textures.
		final Texture rt = this.assetManager.loadTexture("Textures/Rock.PNG");
		rt.setWrap(WrapMode.Repeat);
		final Texture bt = this.assetManager.loadTexture("Textures/BrickWall.jpg");
		bt.setWrap(WrapMode.Repeat);

		final Box cube1Mesh = new Box(1f, 1f, 1f);
		this.cube = new Geometry("My Textured Box", cube1Mesh);
		final Material cube1Mat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		cube1Mat.setColor("Color", new ColorRGBA(.25f, .25f, .25f, .5f));
		cube1Mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		this.cube.setShadowMode(ShadowMode.Receive);

		this.cube.setMaterial(cube1Mat);
		this.cube.setQueueBucket(Bucket.Transparent);
		// rootNode.attachChild(cube);

		this.guiFont = this.assetManager.loadFont("Interface/Fonts/Default.fnt");
		this.position = new BitmapText(this.guiFont, false);
		this.position.setLocalTranslation(0, this.settings.getHeight(), 0);

		this.selectedMaterialN = new BitmapText(this.guiFont, false);
		this.selectedMaterialN.setLocalTranslation((int) (this.settings.getWidth() - this.settings.getWidth() * .4f), this.selectedMaterialN.getLineHeight(), 0);

		this.editorMode = new BitmapText(this.guiFont, false);
		this.editorMode.setLocalTranslation((int) (this.settings.getWidth() - this.settings.getWidth() * .4f), this.selectedMaterialN.getLineHeight() + this.editorMode.getLineHeight() + 10, 0);

		this.direction = new BitmapText(this.guiFont, false);
		this.direction.setLocalTranslation(0, this.settings.getHeight() - this.position.getLineHeight(), 0);

		this.guiNode.attachChild(this.editorMode);
		this.guiNode.attachChild(this.selectedMaterialN);
		this.guiNode.attachChild(this.direction);
		this.guiNode.attachChild(this.position);

		this.input = new Input(this);
	}

	@Override
	public void start() {
		// set some default settings in-case
		// settings dialog is not shown
		this.settings = new AppSettings(true);
		this.settings.setTitle("Dual Contouring Demo");
		// settings.put
		// settings.set
		this.settings.setResolution(800, 600);
		this.showSettings = false;
		// re-setting settings they can have been merged from the registry.
		this.setSettings(this.settings);
		super.start();
	}

	public Image load(final String name) {
		return this.assetManager.loadTexture(name).getImage();
	}

}
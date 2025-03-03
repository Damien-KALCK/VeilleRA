package com.example.veillera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var btnPlaceObject: Button
    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vérifier et demander la permission caméra
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            initAR()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                initAR()
            } else {
                Toast.makeText(this, "L'autorisation caméra est requise pour utiliser l'AR", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initAR() {
        try {
            arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur : Impossible de charger le fragment AR", Toast.LENGTH_LONG).show()
            return
        }

        btnPlaceObject = findViewById(R.id.btnPlaceObject)
        btnPlaceObject.setOnClickListener {
            loadModel()
        }
    }

    private fun loadModel() {
        val modelUri = Uri.parse("file:///android_asset/chaise.glb")

        ModelRenderable.builder()
            .setSource(this, RenderableSource.builder()
                .setSource(this, modelUri, RenderableSource.SourceType.GLB)
                .setScale(0.5f) // Échelle de l'objet
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build())
            .setRegistryId(modelUri)
            .build()
            .thenAccept { renderable ->
                placeObject(renderable)
            }
            .exceptionally {
                Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_LONG).show()
                null
            }
    }

    private fun placeObject(renderable: ModelRenderable) {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val modelNode = TransformableNode(arFragment.transformationSystem)
            modelNode.renderable = renderable
            modelNode.setParent(anchorNode)
            modelNode.select()
        }
    }
}

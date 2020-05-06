package mx.edu.ittepic.ladm_u3_practica2_irmintrujillo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.activity_main2.cantidad
import kotlinx.android.synthetic.main.activity_main2.celular
import kotlinx.android.synthetic.main.activity_main2.descripcion
import kotlinx.android.synthetic.main.activity_main2.domicilio
import kotlinx.android.synthetic.main.activity_main2.nombre
import kotlinx.android.synthetic.main.activity_main2.precio

class Main2Activity : AppCompatActivity() {
    var id = ""
    var baseDatos = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var extras =intent.extras

        id = extras!!.getString("id")!!




        var pre = extras!!.getDouble("precio")!!.toDouble()
        nombre.setText(extras!!.getString("nombre"))
        domicilio.setText(extras!!.getString("domicilio"))
        celular.setText(extras!!.getString("celular"))
        descripcion.setText(extras!!.getString("producto"))
        precio.setText(pre.toString())
        cantidad.setText(extras!!.getLong("cantidad").toString())

        cancelar.setOnClickListener {
            finish()
        }

                actualizar.setOnClickListener {
        baseDatos.collection("restaurante")
            .document(id)
            .update("nombre",nombre.text.toString(),
                "celular",celular.text.toString(),
                "domicilio",domicilio.text.toString(),
                "pedido.cantidad",cantidad.text.toString().toInt(),
                "pedido.producto",descripcion.text.toString(),
                "pedido.precio",precio.text.toString().toFloat()
            )


            .addOnSuccessListener {
                Toast.makeText(this,"ACTUALIZACION REALIZADA", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener{
                Toast.makeText(this,"ERROR NO SE PUEDE ACTUALIZAR , ERROR DE CONEXION", Toast.LENGTH_LONG).show()
            }
    }


    }
}

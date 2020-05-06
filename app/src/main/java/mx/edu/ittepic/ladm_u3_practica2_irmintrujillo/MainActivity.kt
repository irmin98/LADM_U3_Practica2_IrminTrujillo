package mx.edu.ittepic.ladm_u3_practica2_irmintrujillo

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.cantidad
import kotlinx.android.synthetic.main.activity_main.celular
import kotlinx.android.synthetic.main.activity_main.descripcion
import kotlinx.android.synthetic.main.activity_main.domicilio
import kotlinx.android.synthetic.main.activity_main.entregado
import kotlinx.android.synthetic.main.activity_main.nombre
import kotlinx.android.synthetic.main.activity_main.precio
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()

    var dataLista =ArrayList<String>()
    var listaID = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        insertar.setOnClickListener {
            insertarRegistro()
        }

        consultar.setOnClickListener {
            construirDialogo()
        }

        baseRemota.collection("restaurante")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    //si es diferente de null entonces si hay error y entra al if
                    Toast.makeText(this,"ERRROR NO SE PUEDE ACCEDER A CONSULTA",Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                dataLista.clear()
                listaID.clear()
                for(document in querySnapshot!!){
                    var cadena ="Nombre :"+document.getString("nombre") + "\n"+"Cel :"+document.getString("celular") +
                            "\nDomicilio :"+ document.getString("domicilio")
                    dataLista.add(cadena)
                    listaID.add(document.id)
                }
                if(dataLista.size==0){
                    dataLista.add("NO HAY DATA")
                }
                var adaptador =
                    ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataLista)
                lista.adapter=adaptador
            }

        lista.setOnItemClickListener { parent, view, position, id ->
            if(listaID.size==0){
                return@setOnItemClickListener
            }
            AlertaElminarActualizar(position)
        }

    }
    private fun AlertaElminarActualizar(position: Int) {
        AlertDialog.Builder(this).setTitle("ATENCION").setMessage("¿QUE DESEA HACER CON EL CLIENTE? :\n${dataLista[position]}?")
            .setPositiveButton("ELIMINAR"){d,w->
                eliminar(listaID[position])
            }
            .setNegativeButton("ACTUALIZAR"){d,w->
                 llamarVentanaActualizar(listaID[position])


            }
            .setNeutralButton("CANCELAR"){d,w->
            }
            .show()
    }


    private fun llamarVentanaActualizar(idActualizar: String) {
        baseRemota.collection("restaurante")
            .document(idActualizar)
            .get()
            .addOnSuccessListener {
                var v = Intent(this,Main2Activity :: class.java)
                v.putExtra("id",idActualizar)
                v.putExtra("nombre",it.getString("nombre"))
                v.putExtra("domicilio",it.getString("domicilio"))
                v.putExtra("celular",it.getString("celular"))
                v.putExtra("producto",it.getString("pedido.producto"))
                v.putExtra("precio",it.getDouble("pedido.precio")!!.toDouble())
                v.putExtra("cantidad",it.getLong("pedido.cantidad"))
                v.putExtra("entregado",it.getBoolean("pedido.entregado"))

                startActivity(v)
            }
            .addOnFailureListener {
                Toast.makeText(this,"ERROR NO HAY CONEXION DE RED",Toast.LENGTH_LONG).show()
            }
    }

    private fun eliminar(idEliminar: String) {
        baseRemota.collection("restaurante")
            .document(idEliminar)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this,"SE ELIMINO CON EXITO",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,"NO SE PUDO ELIMINAR",Toast.LENGTH_LONG).show()
            }
    }

    private fun construirDialogo() {
        var dialogo =Dialog(this)
        dialogo.setContentView(R.layout.consulta)
        //Declarar los objetos para la interaccion
        var valor =dialogo.findViewById<EditText>(R.id.valor)
        var posicion = dialogo.findViewById<Spinner>(R.id.clave)
        var buscar = dialogo.findViewById<Button>(R.id.buscar)
        var cerrar = dialogo.findViewById<Button>(R.id.cerrar)

        dialogo.show()

        cerrar.setOnClickListener { dialogo.dismiss() }
        buscar.setOnClickListener {
            if(valor.text.isEmpty()){
                Toast.makeText(this,"DEBES PONER UN VALOR PARA BUSCAR",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            realizarConsulta(valor.text.toString(), posicion.selectedItemPosition)
            dialogo.dismiss()
        }
    }

    private fun realizarConsulta(valor: String, clave: Int) {
    /*Claves :
    *  0- nombre cliente
       1 -celular
       2- domicilio
       3 -pedido/producto
       4 -precio
       5- cantidad

    * */
        when(clave){
           0->{consultaNombre(valor)}
            1->{consultaCelular(valor)}
            2->{consultaDomicilio(valor)}
            3->{consultaPedido(valor)}
            4->{consultaPrecio(valor.toFloat())}
            5->{consultaCantidad(valor.toInt())}

        }
    }

    private fun consultaPrecio(valor: Float) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
            .whereEqualTo("pedido.precio", valor.toDouble())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                 //   resultado.setText("ERROR NO HAY CONEXION")
                    return@addSnapshotListener
                }
                var res = ""
                for (document in querySnapshot!!) {
                    res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                            "\nDomicilio :" + document.getString("domicilio") +
                            "\nCelular :" + document.getString("celular") +
                            "\nProducto :" + document.get("pedido.producto") +
                            "\nPrecio :" + document.get("pedido.precio") +
                            "\nCantidad :" + document.get("pedido.cantidad") +
                            "\nEntregado :" + document.get("pedido.entregado")+"\n\n"
                    dataLista.add(res)
                    listaID.add(document.id)
                }
               // resultado.setText(res)
            }
    }

    private fun consultaPedido(valor: String) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
            .whereEqualTo("pedido.producto", valor.toString())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                   // resultado.setText("ERROR NO HAY CONEXION")
                    return@addSnapshotListener
                }
                var res = ""
                for (document in querySnapshot!!) {
                    res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                            "\nDomicilio :" + document.getString("domicilio") +
                            "\nCelular :" + document.getString("celular") +
                            "\nProducto :" + document.get("pedido.producto") +
                            "\nPrecio :" + document.get("pedido.precio") +
                            "\nCantidad :" + document.get("pedido.cantidad") +
                            "\nEntregado :" + document.get("pedido.entregado")+"\n\n"
                    dataLista.add(res)
                    listaID.add(document.id)
                }
              //  resultado.setText(res)
            }
    }

    private fun consultaDomicilio(valor: String) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
            .whereEqualTo("domicilio", valor.toString())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                   // resultado.setText("ERROR NO HAY CONEXION")
                    return@addSnapshotListener
                }
                var res = ""
                for (document in querySnapshot!!) {
                    res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                            "\nDomicilio :" + document.getString("domicilio") +
                            "\nCelular :" + document.getString("celular") +
                            "\nProducto :" + document.get("pedido.producto") +
                            "\nPrecio :" + document.get("pedido.precio") +
                            "\nCantidad :" + document.get("pedido.cantidad") +
                            "\nEntregado :" + document.get("pedido.entregado")+"\n\n"
                    dataLista.add(res)
                    listaID.add(document.id)
                }
               // resultado.setText(res)
            }
    }

    private fun consultaCelular(valor: String) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
            .whereEqualTo("celular", valor.toString())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                 //   resultado.setText("ERROR NO HAY CONEXION")
                    return@addSnapshotListener
                }
                var res = ""
                for (document in querySnapshot!!) {
                    res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                            "\nDomicilio :" + document.getString("domicilio") +
                            "\nCelular :" + document.getString("celular") +
                            "\nProducto :" + document.get("pedido.producto") +
                            "\nPrecio :" + document.get("pedido.precio") +
                            "\nCantidad :" + document.get("pedido.cantidad") +
                            "\nEntregado :" + document.get("pedido.entregado")+"\n\n"
                    dataLista.add(res)
                    listaID.add(document.id)
                }
                //resultado.setText(res)
            }
    }

    private fun consultaNombre(valor: String) {
        dataLista.clear()
        listaID.clear()

        baseRemota.collection("restaurante")
            .whereEqualTo("nombre", valor.toString())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                   // resultado.setText("ERROR NO HAY CONEXION")
                    return@addSnapshotListener
                }
                var res = ""
                for (document in querySnapshot!!) {
                    res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                            "\nDomicilio :" + document.getString("domicilio") +
                            "\nCelular :" + document.getString("celular") +
                            "\nProducto :" + document.get("pedido.producto") +
                            "\nPrecio :" + document.get("pedido.precio") +
                            "\nCantidad :" + document.get("pedido.cantidad") +
                            "\nEntregado :" + document.get("pedido.entregado")+"\n\n"
                    dataLista.add(res)
                    listaID.add(document.id)
                }
                //resultado.setText(res)
            }
    }

    private fun consultaCantidad(valor: Int) {
        dataLista.clear()
        listaID.clear()
        baseRemota.collection("restaurante")
            .whereLessThanOrEqualTo("pedido.cantidad", valor.toInt())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
               //     resultado.setText("ERROR NO HAY CONEXION")
                    return@addSnapshotListener
                }
                var res = ""
                for (document in querySnapshot!!) {
                    res += "ID: " + document.id + "\nNombre: " + document.getString("nombre") +
                            "\nDomicilio :" + document.getString("domicilio") +
                            "\nCelular :" + document.getString("celular") +
                            "\nProducto :" + document.get("pedido.producto") +
                            "\nPrecio :" + document.get("pedido.precio") +
                            "\nCantidad :" + document.get("pedido.cantidad") +
                            "\nEntregado :" + document.get("pedido.entregado")+"\n\n"
                    dataLista.add(res)
                    listaID.add(document.id)

                }
               // resultado.setText(res)
            }
    }

    private fun insertarRegistro() {
        var data = hashMapOf(
            "nombre" to nombre.text.toString(),
            "domicilio" to domicilio.text.toString(),
            "celular" to celular.text.toString(),
            "pedido" to hashMapOf(
                "producto" to descripcion.text.toString(),
                "precio" to precio.text.toString().toFloat(),
                "cantidad" to cantidad.text.toString().toInt(),
                "entregado" to entregado.isChecked
            )

        )
        baseRemota.collection("restaurante").add(data)
            .addOnSuccessListener {
                Toast.makeText(this,"SE CAPTURÓ CORRECTAMENTE", Toast.LENGTH_LONG).show()
                nombre.setText(""); domicilio.setText(""); celular.setText("");descripcion.setText(""); precio.setText("");cantidad.setText("");
            }
            .addOnFailureListener {
                Toast.makeText(this,"ERROR , NO SE CAPTURO",Toast.LENGTH_LONG).show()
            }
    }


}

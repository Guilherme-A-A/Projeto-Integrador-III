package br.com.fiscal.zonaazulfiscal

import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.annotations.SerializedName

/***
 * Supondo que cada vez que vc
 * use uma Function que retorne um documentId (docId) no payload
 * como um Json, basta tratar como um objeto de resposta.
 * Isso organiza o código e pode ser reaproveitado para
 * qualquer function que retorne no payload um docId
 * @author Mateus Dias
 */
class GenericWhereResponse {

    @SerializedName("constraint" )
    var placa: Any? = null;

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericWhereResponse
        if (placa != other.placa) return false
        return true
    }

    override fun hashCode(): Int {
        var result = placa?.hashCode() ?: 0
        result = 31 * result + (placa?.hashCode() ?: 0)
        return result
    }
}
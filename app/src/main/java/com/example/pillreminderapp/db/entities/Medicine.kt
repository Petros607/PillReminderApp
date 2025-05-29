package com.example.pillreminderapp.db.entities

import android.content.Context
import androidx.room.*
import com.example.pillreminderapp.R

enum class DosageForm {
    TABLET,
    CAPSULE,
    PROCEDURE,
    SOLUTION, //раствор
    DROPLET, //капли
    LIQUID,
    OINTMENT, //мазь
    SPRAY,
    OTHER;

    fun getLocalizedName(context: Context): String {
        return when (this) {
            TABLET -> context.getString(R.string.dosage_tablet)
            CAPSULE -> context.getString(R.string.dosage_capsule)
            PROCEDURE -> context.getString(R.string.dosage_procedure)
            SOLUTION -> context.getString(R.string.dosage_solution)
            DROPLET -> context.getString(R.string.dosage_droplet)
            LIQUID -> context.getString(R.string.dosage_liquid)
            OINTMENT -> context.getString(R.string.dosage_ointment)
            SPRAY -> context.getString(R.string.dosage_spray)
            OTHER -> context.getString(R.string.dosage_other)
        }
    }
}

enum class ActiveSubstance(val displayName: String) {
    PARACETAMOL("Парацетамол"),
    IBUPROFEN("Ибупрофен"),
    AMOXICILLIN("Амоксициллин"),
    DROTAVERINE("Но-шпа"),
    ASPIRIN("Аспирин"),
    LOPERAMIDE("Лоперамид"),
    OMEPRAZOLE("Омепразол"),
    METFORMIN("Метформин"),
    ATORVASTATIN("Аторвастатин"),
    AMLODIPINE("Амлодипин"),
    BISOPROLOL("Бисопролол"),
    LOSARTAN("Лозартан"),
    CEFTRIAXONE("Цефтриаксон"),
    AZITHROMYCIN("Азитромицин"),
    LEVOFLOXACIN("Левофлоксацин"),
    METRONIDAZOLE("Метронидазол"),
    DICLOFENAC("Диклофенак"),
    KETOROLAC("Кеторолак"),
    PANTOPRAZOLE("Пантопразол"),
    SIMVASTATIN("Симвастатин"),
    VALSARTAN("Валсартан"),
    ENALAPRIL("Эналаприл"),
    FEXOFENADINE("Фексофенадин"),
    LORATADINE("Лоратадин"),
    CETIRIZINE("Цетиризин"),
    DEXAMETHASONE("Дексаметазон"),
    PREDNISOLONE("Преднизолон"),
    INSULIN("Инсулин"),
    SALBUTAMOL("Сальбутамол"),
    BUDESONIDE("Будесонид"),
    OTHER("Другое");

    override fun toString(): String = displayName
}

enum class Manufacturer(val displayName: String) {
    BAYER("Байер"),
    PFIZER("Пфайзер"),
    GEDEON("Гедеон Рихтер"),
    TEVA("Тева"),
    NOVARTIS("Новартис"),
    SANOFI("Санофи"),
    ROCHE("Рош"),
    MERCK("Мерк"),
    ASTRAZENECA("АстраЗенека"),
    JOHNSON_JOHNSON("Джонсон"),
    ABBOTT("Эбботт"),
    SERVIER("Сервье"),
    TAKEDA("Такеда"),
    ELI_LILLY("Эли Лилли"),
    BIOCAD("Биокад"),
    PHARMSTANDARD("Фармстандарт"),
    VALENTA("Валента Фарма"),
    OZON("Озон"),
    VEROPHARM("Верофарм"),
    SOTEX("Сотекс"),
    AKRIKHIN("Акрихин"),
    SYNTHESIS("Синтез"),
    DALHIMFARM("Дальхимфарм"),
    PHARMASOFT("Фармасофт"),
    OBLENKARF("Оболенское"),
    MOSHIMFARM("Мосхимфарм"),
    OTHER("Другое");

    override fun toString(): String = displayName
}


@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val substance: ActiveSubstance,
    val manufacturer: Manufacturer,

    @ColumnInfo(name = "dosage_form")
    val dosageForm: DosageForm
)
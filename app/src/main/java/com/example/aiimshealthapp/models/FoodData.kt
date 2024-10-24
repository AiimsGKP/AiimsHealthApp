package com.example.aiimshealthapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class FoodData(
    val code: String,
    val product: Product
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(Product::class.java.classLoader) ?: Product("", "", emptyList(), "", "", emptyList(), "", "", emptyList(), "", Nutriments(), NutriScore(), "")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeParcelable(product, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FoodData> {
        override fun createFromParcel(parcel: Parcel): FoodData {
            return FoodData(parcel)
        }

        override fun newArray(size: Int): Array<FoodData?> {
            return arrayOfNulls(size)
        }
    }
}

data class Product(
    val brands: String,
    val product_name: String,
    val countries_tags: List<String>,
    val image_front_url: String,
    val image_ingredients_url: String,
    val ingredients: List<Ingredients>,
    val ingredients_text: String,
    val nova_group: String,
    val nova_groups_tags: List<String>,
    val nova_group_debug: String,
    val nutriments: Nutriments,
    val nutriscore: NutriScore,
    val quantity: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(Ingredients.CREATOR) ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readParcelable(Nutriments::class.java.classLoader) ?: Nutriments(),
        parcel.readParcelable(NutriScore::class.java.classLoader) ?: NutriScore(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(brands)
        parcel.writeString(product_name)
        parcel.writeStringList(countries_tags)
        parcel.writeString(image_front_url)
        parcel.writeString(image_ingredients_url)
        parcel.writeTypedList(ingredients)
        parcel.writeString(ingredients_text)
        parcel.writeString(nova_group)
        parcel.writeStringList(nova_groups_tags)
        parcel.writeString(nova_group_debug)
        parcel.writeParcelable(nutriments, flags)
        parcel.writeParcelable(nutriscore, flags)
        parcel.writeString(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}

data class Ingredients(
    val title: String,
    val percent: String,
    val vegan: String,
    val vegetarian: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(percent)
        parcel.writeString(vegan)
        parcel.writeString(vegetarian)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ingredients> {
        override fun createFromParcel(parcel: Parcel): Ingredients {
            return Ingredients(parcel)
        }

        override fun newArray(size: Int): Array<Ingredients?> {
            return arrayOfNulls(size)
        }
    }
}

data class Nutriments(
    val carbohydrates: Double = 0.0,
    val carbohydrates_unit: String = "",
    @SerializedName("energy-kcal") val energyKcal: Double = 0.0,
    val energy_unit: String = "",
    val fat: Double = 0.0,
    val fat_unit: String = "",
    val proteins: Double = 0.0,
    val proteins_unit: String = "",
    val salt: Double = 0.0,
    val salt_unit: String = "",
    val sodium: Double = 0.0,
    val sodium_unit: String = "",
    val sugars: Double = 0.0,
    val sugars_unit: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(carbohydrates)
        parcel.writeString(carbohydrates_unit)
        parcel.writeDouble(energyKcal)
        parcel.writeString(energy_unit)
        parcel.writeDouble(fat)
        parcel.writeString(fat_unit)
        parcel.writeDouble(proteins)
        parcel.writeString(proteins_unit)
        parcel.writeDouble(salt)
        parcel.writeString(salt_unit)
        parcel.writeDouble(sodium)
        parcel.writeString(sodium_unit)
        parcel.writeDouble(sugars)
        parcel.writeString(sugars_unit)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Nutriments> {
        override fun createFromParcel(parcel: Parcel): Nutriments {
            return Nutriments(parcel)
        }

        override fun newArray(size: Int): Array<Nutriments?> {
            return arrayOfNulls(size)
        }
    }
}

data class NutriScore(
    val `2021`: NutriScoreYear = NutriScoreYear(),
    val `2023`: NutriScoreYear = NutriScoreYear()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(NutriScoreYear::class.java.classLoader) ?: NutriScoreYear(),
        parcel.readParcelable(NutriScoreYear::class.java.classLoader) ?: NutriScoreYear()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(`2021`, flags)
        parcel.writeParcelable(`2023`, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NutriScore> {
        override fun createFromParcel(parcel: Parcel): NutriScore {
            return NutriScore(parcel)
        }

        override fun newArray(size: Int): Array<NutriScore?> {
            return arrayOfNulls(size)
        }
    }
}

data class NutriScoreYear(
    val data: NutritionalData = NutritionalData(),
    val grade: String = "",
    val score: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(NutritionalData::class.java.classLoader) ?: NutritionalData(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(data, flags)
        parcel.writeString(grade)
        parcel.writeInt(score)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NutriScoreYear> {
        override fun createFromParcel(parcel: Parcel): NutriScoreYear {
            return NutriScoreYear(parcel)
        }

        override fun newArray(size: Int): Array<NutriScoreYear?> {
            return arrayOfNulls(size)
        }
    }
}

data class NutritionalData(
    val components: NutritionalComponents = NutritionalComponents()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(NutritionalComponents::class.java.classLoader) ?: NutritionalComponents()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(components, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NutritionalData> {
        override fun createFromParcel(parcel: Parcel): NutritionalData {
            return NutritionalData(parcel)
        }

        override fun newArray(size: Int): Array<NutritionalData?> {
            return arrayOfNulls(size)
        }
    }
}

data class NutritionalComponents(
    val negative: List<NutrientDetail> = emptyList(),
    val positive: List<NutrientDetail> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(NutrientDetail.CREATOR) ?: emptyList(),
        parcel.createTypedArrayList(NutrientDetail.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(negative)
        parcel.writeTypedList(positive)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NutritionalComponents> {
        override fun createFromParcel(parcel: Parcel): NutritionalComponents {
            return NutritionalComponents(parcel)
        }

        override fun newArray(size: Int): Array<NutritionalComponents?> {
            return arrayOfNulls(size)
        }
    }
}

data class NutrientDetail(
    val id: String = "",
    val points: String = "",
    val points_max: String = "",
    val unit: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(points)
        parcel.writeString(points_max)
        parcel.writeString(unit)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NutrientDetail> {
        override fun createFromParcel(parcel: Parcel): NutrientDetail {
            return NutrientDetail(parcel)
        }

        override fun newArray(size: Int): Array<NutrientDetail?> {
            return arrayOfNulls(size)
        }
    }
}

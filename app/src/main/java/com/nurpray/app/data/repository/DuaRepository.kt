package com.nurpray.app.data.repository

data class DuaItem(
    val id: String,
    val category: String,
    val title: String,
    val arabicText: String,
    val transliteration: String,
    val italianTranslation: String,
    val source: String,
    val targetCount: Int = 1
)

object DuaRepository {

    val categories = listOf(
        "Tutte",
        "Mattina & Sera",
        "Digiuno & Iftar",
        "Preghiera",
        "Protezione",
        "Perdono",
        "Famiglia & Genitori"
    )

    val duas: List<DuaItem> = listOf(
        // Digiuno & Iftar
        DuaItem(
            id = "iftar_1",
            category = "Digiuno & Iftar",
            title = "Rottura del Digiuno (Iftar)",
            arabicText = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ",
            transliteration = "Dhahaba adh-dhama'u wabtallatil-'urooqu wa thabatal-ajru in sha Allah",
            italianTranslation = "La sete se n'è andata, le vene si sono inumidite e la ricompensa è confermata, se Allah vuole.",
            source = "Abu Dawud (2357)",
            targetCount = 1
        ),
        DuaItem(
            id = "suhoor_imsak",
            category = "Digiuno & Iftar",
            title = "Intenzione del Digiuno (Niyyah)",
            arabicText = "وَبِصَوْمِ غَدٍ نَّوَيْتُ مِنْ شَهْرِ رَمَضَانَ",
            transliteration = "Wa bisawmi ghadin nawaytu min shahri Ramadan",
            italianTranslation = "Intendo digiunare domani per il mese di Ramadan per la causa di Allah l'Altissimo.",
            source = "Sunnah",
            targetCount = 1
        ),

        // Mattina & Sera
        DuaItem(
            id = "morning_evening_1",
            category = "Mattina & Sera",
            title = "Padrone del Giorno (Sayyid al-Istighfar)",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u bidhanbi faghfir li fa-innahu la yaghfiru adh-dhunooba illa Anta.",
            italianTranslation = "O Allah, Tu sei il mio Signore, non c'è altro dio all'infuori di Te. Tu mi hai creato e io sono Tuo servo; mantengo il Tuo patto e la Tua promessa fin dove posso. Mi rifugio in Te dal male che ho commesso. Riconosco la Tua grazia su di me e riconosco il mio peccato: perdonami, poiché nessuno rimette i peccati eccetto Te.",
            source = "Sahih al-Bukhari (6306)",
            targetCount = 1
        ),
        DuaItem(
            id = "morning_2",
            category = "Mattina & Sera",
            title = "Gratitudine e Benedizione del Mattino",
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Asbahna wa-asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadeer.",
            italianTranslation = "Siamo giunti al mattino e il regno appartiene ad Allah. La lode è per Allah. Non c'è divinità all'infuori di Allah, Unico, senza associati. A Lui appartiene la sovranità, a Lui la lode, ed Egli su ogni cosa ha potere.",
            source = "Sahih Muslim (2723)",
            targetCount = 1
        ),

        // Preghiera
        DuaItem(
            id = "prayer_after_adhan",
            category = "Preghiera",
            title = "Supplica dopo l'Adhan",
            arabicText = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّداً الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَاماً مَحْمُوداً الَّذِي وَعَدْتَهُ",
            transliteration = "Allahumma Rabba hadhihid-da'watit-tammah, was-salatil-qa'imah, ati Muhammadanil-wasilata wal-fadilah, wab'ath-hu maqaman mahmoodanilladhi wa'adtah.",
            italianTranslation = "O Allah, Signore di questo richiamo perfetto e della preghiera che si compie, concedi a Muhammad la Wasilah e la virtù, e risuscitalo nella nobile stazione che gli hai promesso.",
            source = "Sahih al-Bukhari (614)",
            targetCount = 1
        ),
        DuaItem(
            id = "prayer_prostration",
            category = "Preghiera",
            title = "Supplica nel Sujud (Prostrazione)",
            arabicText = "سُبْحَانَ رَبِّيَ الأَعْلَى",
            transliteration = "Subhana Rabbiyal-A'la",
            italianTranslation = "Gloria al mio Signore, l'Altissimo.",
            source = "Sunan Abi Dawud (871)",
            targetCount = 3
        ),

        // Protezione
        DuaItem(
            id = "protection_evil",
            category = "Protezione",
            title = "Protezione da ogni Male e Infortunio",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahilladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Aleem.",
            italianTranslation = "Nel nome di Allah, col Cui Nome nulla può nuocere sulla terra né nei cieli, ed Egli è Colui che tutto ascolta e tutto conosce.",
            source = "Abu Dawud (5088) e Tirmidhi (3388)",
            targetCount = 3
        ),

        // Famiglia & Genitori
        DuaItem(
            id = "parents_dua",
            category = "Famiglia & Genitori",
            title = "Misericordia per i Genitori",
            arabicText = "رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliteration = "Rabbi-rhamhuma kama rabbayani sagheera.",
            italianTranslation = "Mio Signore, abbi pietà di loro come essi ebbero cura di me quand'ero piccino.",
            source = "Sacro Corano (Al-Isra, 17:24)",
            targetCount = 3
        ),

        // Perdono
        DuaItem(
            id = "forgiveness_1",
            category = "Perdono",
            title = "Costante Richiesta di Perdono",
            arabicText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullahal-'Azeem alladhi la ilaha illa Huwal-Hayyul-Qayyoomu wa atoobu ilayh.",
            italianTranslation = "Chiedo perdono ad Allah il Maestoso, oltre al Quale non c'è dio, il Vivente, l'Assoluto Custode, e a Lui mi rivolgo pentito.",
            source = "At-Tirmidhi (3577)",
            targetCount = 3
        )
    )
}

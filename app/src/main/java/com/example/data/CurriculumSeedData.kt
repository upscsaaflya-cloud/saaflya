package com.example.data

import com.example.data.model.CurrentAffairsCategory
import com.example.data.model.CurrentAffairsItem
import com.example.data.model.ExamCategory
import com.example.data.model.ExamSubject
import com.example.data.model.MainsPracticePrompt
import com.example.data.model.McqQuestion
import com.example.data.model.SyllabusTopicTemplate
import com.example.data.network.CurrentAffairsApiService

object CurriculumSeedData {

    val curatedMcqs: List<McqQuestion> = listOf(
        // --- GPSC: INDIAN POLITY ---
        McqQuestion(
            id = "gpsc_pol_1",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.POLITY.id,
            questionTextEn = "Under which Constitutional Amendment was the Panchayati Raj system granted constitutional status in India, and which part of the Constitution deals with it?",
            questionTextGu = "ભારતમાં કયા બંધારણીય સુધારા હેઠળ પંચાયતી રાજ વ્યવસ્થાને બંધારણીય દરજ્જો આપવામાં આવ્યો અને બંધારણનો કયો ભાગ તેની સાથે સંબંધિત છે?",
            optionsEn = listOf(
                "73rd Amendment Act, 1992 — Part IX",
                "74th Amendment Act, 1992 — Part IX-A",
                "42nd Amendment Act, 1976 — Part IV",
                "86th Amendment Act, 2002 — Part III"
            ),
            optionsGu = listOf(
                "૭૩મો બંધારણીય સુધારો, ૧૯૯૨ — ભાગ IX",
                "૭૪મો બંધારણીય સુધારો, ૧૯૯૨ — ભાગ IX-A",
                "૪૨મો બંધારણીય સુધારો, ૧૯૭૬ — ભાગ IV",
                "૮૬મો બંધારણીય સુધારો, ૨૦૦૨ — ભાગ III"
            ),
            correctOptionIndex = 0,
            explanationEn = "The 73rd Constitutional Amendment Act, 1992 added Part IX (Articles 243 to 243-O) and the 11th Schedule (29 subjects) to the Constitution. Gujarat implemented the Gujarat Panchayats Act, 1993 featuring the 3-tier Gram, Taluka, and Jilla Panchayat structure.",
            explanationGu = "૭૩મા બંધારણીય સુધારા અધિનિયમ, ૧૯૯૨ દ્વારા બંધારણમાં ભાગ IX (અનુચ્છેદ ૨૪૩ થી ૨૪૩-O) અને ૧૧મી અનુસૂચિ (૨૯ વિષયો) ઉમેરવામાં આવી. ગુજરાતમાં ત્રિ-સ્તરીય પંચાયતી રાજ (ગ્રામ, તાલુકા અને જિલ્લા પંચાયત) માટે ગુજરાત પંચાયત અધિનિયમ, ૧૯૯૩ અમલમાં છે.",
            referenceNote = "Indian Polity (M. Laxmikanth) Ch. 38 & Gujarat Panchayats Act, 1993",
            topicTagEn = "Panchayati Raj & Local Governance",
            topicTagGu = "પંચાયતી રાજ અને સ્થાનિક સ્વરાજ્ય",
            difficulty = "High-Yield PYQ",
            keyTakeawaysEn = listOf(
                "Article 243A: Gram Sabha; Article 243D: Reservation of seats (min 1/3rd for women, 50% in Gujarat).",
                "Article 243I: State Finance Commission constituted every 5 years by the Governor.",
                "Article 243K: State Election Commission conducts Panchayat elections."
            ),
            keyTakeawaysGu = listOf(
                "અનુચ્છેદ ૨૪૩-A: ગ્રામ સભા; અનુચ્છેદ ૨૪૩-D: બેઠકોનું અનામત (ગુજરાતમાં મહિલાઓ માટે ૫૦% અનામત).",
                "અનુચ્છેદ ૨૪૩-I: દર ૫ વર્ષે રાજ્યપાલ દ્વારા રાજ્ય નાણા પંચની રચના.",
                "અનુચ્છેદ ૨૪૩-K: રાજ્ય ચૂંટણી પંચ દ્વારા પંચાયતની ચૂંટણીઓનું સંચાલન."
            )
        ),
        McqQuestion(
            id = "gpsc_pol_2",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.POLITY.id,
            questionTextEn = "With reference to the Governor of a State, which Article of the Indian Constitution empowers the Governor to reserve a Bill for the consideration of the President?",
            questionTextGu = "રાજ્યના રાજ્યપાલના સંદર્ભમાં, ભારતીય બંધારણનો કયો અનુચ્છેદ રાજ્યપાલને કોઈ વિધેયક (બિલ) રાષ્ટ્રપતિની વિચારણા માટે અનામત રાખવાની સત્તા આપે છે?",
            optionsEn = listOf(
                "Article 163",
                "Article 200",
                "Article 213",
                "Article 356"
            ),
            optionsGu = listOf(
                "અનુચ્છેદ ૧૬૩",
                "અનુચ્છેદ ૨૦૦",
                "અનુચ્છેદ ૨૧૩",
                "અનુચ્છેદ ૩૫૬"
            ),
            correctOptionIndex = 1,
            explanationEn = "Under Article 200, when a Bill passed by the State Legislature is presented to the Governor, the Governor may give assent, withhold assent, return the bill (if not a Money Bill), or reserve the bill for the consideration of the President.",
            explanationGu = "અનુચ્છેદ ૨૦૦ મુજબ, જ્યારે રાજ્ય વિધાનમંડળ દ્વારા પસાર કરાયેલું વિધેયક રાજ્યપાલ સમક્ષ રજૂ કરવામાં આવે ત્યારે તેઓ સંમતિ આપી શકે, સંમતિ રોકી શકે, પુનર્વિચારણા માટે પરત મોકલી શકે અથવા રાષ્ટ્રપતિની વિચારણા માટે અનામત રાખી શકે છે.",
            referenceNote = "Constitution of India — Part VI, Article 200 (State Executive & Legislature)",
            topicTagEn = "State Executive & Legislature",
            topicTagGu = "રાજ્ય કારોબારી અને ધારાસભા",
            difficulty = "Conceptual",
            keyTakeawaysEn = listOf(
                "Article 200 deals with Governor's options on State Bills; Article 201 deals with President's consideration of reserved bills.",
                "Article 213 empowers the Governor to promulgate Ordinances when the State Legislature is not in session."
            ),
            keyTakeawaysGu = listOf(
                "અનુચ્છેદ ૨૦૦: રાજ્યના વિધેયકો પર રાજ્યપાલની સંમતિ; અનુચ્છેદ ૨૦૧: અનામત રાખેલા વિધેયક પર રાષ્ટ્રપતિનો નિર્ણય.",
                "અનુચ્છેદ ૨૧૩: વિધાનસભા સત્રમાં ન હોય ત્યારે રાજ્યપાલની વટહુકમ બહાર પાડવાની સત્તા."
            )
        ),
        McqQuestion(
            id = "gpsc_pol_3",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.POLITY.id,
            questionTextEn = "Which of the following Constitutional Writs is issued by the Supreme Court (Article 32) or High Court (Article 226) to a public official asking 'By what authority or warrant' they hold a public office?",
            questionTextGu = "સુપ્રીમ કોર્ટ (અનુચ્છેદ ૩૨) અથવા હાઈકોર્ટ (અનુચ્છેદ ૨૨૬) દ્વારા કઈ બંધારણીય રિટ જાહેર હોદ્દો ધરાવતી વ્યક્તિને 'કઈ સત્તા કે અધિકારથી' તેઓ તે પદ ધરાવે છે તેની તપાસ કરવા માટે જારી કરવામાં આવે છે?",
            optionsEn = listOf(
                "Quo-Warranto",
                "Mandamus",
                "Certiorari",
                "Habeas Corpus"
            ),
            optionsGu = listOf(
                "ક્વો-વોરંટો (અધિકારપૃચ્છા)",
                "મેન્ડેમસ (પરમાદેશ)",
                "સર્શિયોરરી (ઉત્પ્રેષણ)",
                "હેબિયસ કોર્પસ (બંદી પ્રત્યક્ષીકરણ)"
            ),
            correctOptionIndex = 0,
            explanationEn = "Quo-Warranto literally means 'by what authority or warrant'. It is issued by the court to enquire into the legality of claim of a person to a public office created by statute or the Constitution. Unlike the other four writs, Quo-Warranto can be sought by any interested person and not necessarily the aggrieved person.",
            explanationGu = "'ક્વો-વોરંટો' (અધિકારપૃચ્છા) નો શાબ્દિક અર્થ 'કયા અધિકારથી' થાય છે. તે કોઈ વ્યક્તિ દ્વારા ગેરકાયદેસર રીતે જાહેર હોદ્દો પચાવી પાડવામાં આવે ત્યારે તેને રોકવા માટે જારી કરવામાં આવે છે.",
            referenceNote = "Indian Polity — Fundamental Rights (Articles 32 & 226)",
            topicTagEn = "Fundamental Rights & Writs",
            topicTagGu = "મૂળભૂત અધિકારો અને બંધારણીય રિટ",
            difficulty = "Conceptual",
            keyTakeawaysEn = listOf(
                "Dr. B.R. Ambedkar called Article 32 the 'Heart and Soul of the Constitution'.",
                "High Courts writ jurisdiction under Article 226 is wider than Article 32 because it covers both Fundamental Rights and ordinary legal rights."
            ),
            keyTakeawaysGu = listOf(
                "ડૉ. બાબાસાહેબ આંબેડકરે અનુચ્છેદ ૩૨ને 'બંધારણનો આત્મા અને હૃદય' ગણાવ્યો છે.",
                "હાઈકોર્ટનું રિટ ક્ષેત્રાધિકાર (અનુચ્છેદ ૨૨૬) મૂળભૂત અધિકારો ઉપરાંત અન્ય કાયદાકીય અધિકારોને પણ આવરી લે છે."
            )
        ),

        // --- GPSC: GUJARAT HISTORY & CULTURE ---
        McqQuestion(
            id = "gpsc_hist_1",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GUJARAT_HISTORY.id,
            questionTextEn = "Which Harappan metropolitan city in Kutch, Gujarat is renowned for its unique three-division town planning (Citadel, Middle Town, Lower Town) and advanced rock-cut water reservoirs?",
            questionTextGu = "ગુજરાતના કચ્છમાં આવેલું કયું હડપ્પીય મહાનગર તેના વિશિષ્ટ ત્રિ-સ્તરીય નગર આયોજન (ગઢ, મધ્ય નગર, નીચલું નગર) અને ખડકોમાંથી કોતરેલા જળ સંગ્રહસ્થાનો માટે પ્રખ્યાત છે?",
            optionsEn = listOf(
                "Lothal",
                "Dholavira (Kotada Timba)",
                "Rangpur",
                "Surkotada"
            ),
            optionsGu = listOf(
                "લોથલ",
                "ધોળાવીરા (કોટડા ટીંબા)",
                "રંગપુર",
                "સુરકોટડા"
            ),
            correctOptionIndex = 1,
            explanationEn = "Dholavira (located on Khadir Bet island in Bhachau Taluka of Kutch) was inscribed as India's 40th UNESCO World Heritage Site. Unlike other Harappan cities divided into two parts, Dholavira is divided into three sections and features 16+ cascading water reservoirs and the world's earliest signboard with 10 large Indus script glyphs.",
            explanationGu = "કચ્છના ભચાઉ તાલુકાના ખદીર બેટમાં આવેલું ધોળાવીરા ભારતનું ૪૦મું યુનેસ્કો વર્લ્ડ હેરિટેજ સ્થળ છે. અન્ય હડપ્પીય નગરોથી વિપરીત તે ત્રણ ભાગમાં વહેંચાયેલું છે અને તેની જળ સંચય પ્રણાલી તથા ૧૦ અક્ષરોનું સાઈનબોર્ડ વિશ્વભરમાં પ્રખ્યાત છે.",
            referenceNote = "GPSC Class 1-2 Syllabus: Indus Valley Civilization in Gujarat (GCERT / Cultural Heritage)",
            topicTagEn = "Indus Valley Sites in Gujarat",
            topicTagGu = "ગુજરાતમાં સિંધુ ખીણની સંસ્કૃતિ",
            difficulty = "High-Yield PYQ",
            keyTakeawaysEn = listOf(
                "Discovered by J.P. Joshi (1967-68) and excavated extensively by R.S. Bisht.",
                "Situated between two seasonal streams: Mansar in the north and Manhar in the south."
            ),
            keyTakeawaysGu = listOf(
                "ધોળાવીરાની શોધ જે.પી. જોશી (૧૯૬૭-૬૮) દ્વારા અને વિસ્તૃત ઉત્ખનન આર.એસ. બિષ્ટ દ્વારા કરવામાં આવ્યું હતું.",
                "તે ઉત્તરમાં માનસર અને દક્ષિણમાં મનહર નામના બે મોસમી વહેળાઓ વચ્ચે આવેલું છે."
            )
        ),
        McqQuestion(
            id = "gpsc_hist_2",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GUJARAT_HISTORY.id,
            questionTextEn = "Who commissioned the construction of the famous 'Rani ki Vav' stepwell in Patan during the Solanki (Chalukya) dynasty, and in which style of architecture is it built?",
            questionTextGu = "સોલંકી (ચાલુક્ય) વંશ દરમિયાન પાટણમાં પ્રખ્યાત 'રાણીની વાવ'નું નિર્માણ કોણે કરાવ્યું હતું અને તે કઈ સ્થાપત્ય શૈલીમાં બનેલી છે?",
            optionsEn = listOf(
                "Queen Udayamati in memory of Bhimdev I — Maru-Gurjara Style",
                "Queen Minaldevi during Siddharaj Jaisinh's reign — Nagara Style",
                "Queen Naikidevi — Vesara Style",
                "King Kumarpal — Dravida Style"
            ),
            optionsGu = listOf(
                "રાણી ઉદયમતીએ ભીમદેવ પ્રથમની યાદમાં — મારુ-ગુર્જર શૈલી",
                "રાણી મીનળદેવીએ સિદ્ધરાજ જયસિંહના સમયમાં — નાગર શૈલી",
                "રાણી નાયિકાદેવીએ — વેસર શૈલી",
                "રાજા કુમારપાળે — દ્રવિડ શૈલી"
            ),
            correctOptionIndex = 0,
            explanationEn = "Rani ki Vav was constructed in the 11th century by Queen Udayamati as a memorial to her husband King Bhimdev I of the Solanki dynasty. Built in the Maru-Gurjara architectural style as an inverted temple sanctifying water, it is a seven-storey Jaya-type stepwell on the banks of the Saraswati River.",
            explanationGu = "૧૧મી સદીમાં સોલંકી રાજા ભીમદેવ પ્રથમની યાદમાં રાણી ઉદયમતીએ સરસ્વતી નદીના કિનારે રાણીની વાવ બંધાવી હતી. તે મારુ-ગુર્જર સ્થાપત્ય શૈલીમાં બંધાયેલી સાત માળની 'જયા' પ્રકારની વાવ છે.",
            referenceNote = "Cultural Heritage of Gujarat — Solanki Era Architecture",
            topicTagEn = "Solanki Era Architecture",
            topicTagGu = "સોલંકી કાળનું સ્થાપત્ય",
            difficulty = "Prelims GS"
        ),

        // --- GPSC: GEOGRAPHY ---
        McqQuestion(
            id = "gpsc_geo_1",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GEOGRAPHY.id,
            questionTextEn = "Which river in Gujarat crosses the Tropic of Cancer (23.5° N) twice, and through how many districts of Gujarat does the Tropic of Cancer pass?",
            questionTextGu = "ગુજરાતમાં કર્કવૃત્ત (૨૩.૫° ઉત્તર અક્ષાંશ) કેટલા જિલ્લાઓમાંથી પસાર થાય છે અને કઈ નદી તેને બે વાર ઓળંગે છે?",
            optionsEn = listOf(
                "Passes through 6 districts; Mahi River crosses it twice",
                "Passes through 8 districts; Narmada River crosses it twice",
                "Passes through 5 districts; Sabarmati River crosses it twice",
                "Passes through 7 districts; Tapi River crosses it twice"
            ),
            optionsGu = listOf(
                "૬ જિલ્લાઓમાંથી પસાર થાય છે; મહી નદી તેને બે વાર ઓળંગે છે",
                "૮ જિલ્લાઓમાંથી પસાર થાય છે; નર્મદા નદી તેને બે વાર ઓળંગે છે",
                "૫ જિલ્લાઓમાંથી પસાર થાય છે; સાબરમતી નદી તેને બે વાર ઓળંગે છે",
                "૭ જિલ્લાઓમાંથી પસાર થાય છે; તાપી નદી તેને બે વાર ઓળંગે છે"
            ),
            correctOptionIndex = 0,
            explanationEn = "The Tropic of Cancer passes through 6 districts of Gujarat (Kutch, Patan, Mehsana, Sabarkantha, Aravalli, and Gandhinagar). The Mahi River originates in Madhya Pradesh, flows north into Rajasthan (crossing Tropic of Cancer first time), and turns southwest into Gujarat (crossing Tropic of Cancer a second time).",
            explanationGu = "કર્કવૃત્ત ગુજરાતના ૬ જિલ્લાઓ (કચ્છ, પાટણ, મહેસાણા, સાબરકાંઠા, અરવલ્લી અને ગાંધીનગર) માંથી પસાર થાય છે. મહી નદી કર્કવૃત્તને બે વાર ઓળંગનારી ભારતની એકમાત્ર નદી છે.",
            referenceNote = "Physical Geography of Gujarat — GCERT Standard 9-11 & GPSC GS-1",
            topicTagEn = "Physiography & Drainage of Gujarat",
            topicTagGu = "ગુજરાતનું ભૂપૃષ્ઠ અને નદી તંત્ર",
            difficulty = "High-Yield PYQ",
            keyTakeawaysEn = listOf(
                "6 Districts on Tropic of Cancer (West to East): Kutch, Patan, Mehsana, Sabarkantha, Aravalli, Gandhinagar.",
                "Kadana and Wanakbori dams are major hydro/irrigation projects on the Mahi River in Gujarat."
            ),
            keyTakeawaysGu = listOf(
                "કર્કવૃત્ત પરના ૬ જિલ્લાઓ: કચ્છ, પાટણ, મહેસાણા, સાબરકાંઠા, અરવલ્લી અને ગાંધીનગર.",
                "મહી નદી પર ગુજરાતમાં કડાણા અને વણાકબોરી બંધ આવેલા છે."
            )
        ),
        McqQuestion(
            id = "gpsc_geo_2",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GEOGRAPHY.id,
            questionTextEn = "With reference to the Narmada River basin in Gujarat, which of the following statements is correct regarding its geological course and tributaries?",
            questionTextGu = "ગુજરાતમાં નર્મદા નદી બેસિનના સંદર્ભમાં, તેના ભૂસ્તરશાસ્ત્રીય વહેણ અને સહાયક નદીઓ અંગે નીચેનામાંથી કયું વિધાન સાચું છે?",
            optionsEn = listOf(
                "It flows westward through a fault-bounded rift valley between the Vindhya and Satpura ranges, and Orsang is its major right-bank tributary in Gujarat",
                "It forms an extensive bird-foot delta in the Gulf of Kutch",
                "It originates from the Mahabaleshwar plateau in the Western Ghats",
                "Shetrunji and Bhadar are its primary tributaries joining near Bharuch"
            ),
            optionsGu = listOf(
                "તે વિંધ્ય અને સાતપુડા પર્વતમાળા વચ્ચેની ફાટખીણ (Rift Valley) માંથી પશ્ચિમ તરફ વહે છે અને ઓરસંગ ગુજરાતમાં તેની મુખ્ય જમણા કાંઠાની સહાયક નદી છે",
                "તે કચ્છના અખાતમાં વિશાળ મુખત્રિકોણ પ્રદેશ રચે છે",
                "તે પશ્ચિમ ઘાટના મહાબળેશ્વર ઉચ્ચપ્રદેશમાંથી નીકળે છે",
                "શેત્રુંજી અને ભાદર તેની મુખ્ય સહાયક નદીઓ છે"
            ),
            correctOptionIndex = 0,
            explanationEn = "The Narmada originates at Amarkantak Plateau (Madhya Pradesh) and flows westward through a structural rift valley between the Vindhya and Satpura ranges before entering the Arabian Sea via an estuary at the Gulf of Khambhat near Aliabet (Bharuch). Orsang (joining at Chandod) is its largest right-bank tributary in Gujarat, while Karjan joins on the left bank.",
            explanationGu = "નર્મદા નદી અમરકંટક ઉચ્ચપ્રદેશમાંથી નીકળીને વિંધ્ય અને સાતપુડા વચ્ચેની ફાટખીણમાંથી પશ્ચિમ તરફ વહે છે અને ભરૂચ પાસે ખંભાતના અખાતમાં અલિયાબેટ નજીક સમુદ્રને મળે છે. ચાંદોદ ખાતે મળતી ઓરસંગ નદી ગુજરાતમાં તેની જમણા કાંઠાની સૌથી મોટી સહાયક નદી છે, જ્યારે કરજણ ડાબા કાંઠે મળે છે.",
            referenceNote = "Geography of Gujarat — River Systems (GCERT / Granthnirman Board)",
            topicTagEn = "Drainage System & Rift Valleys",
            topicTagGu = "નદી તંત્ર અને ફાટખીણ ભૂસ્તરશાસ્ત્ર",
            difficulty = "Conceptual",
            keyTakeawaysEn = listOf(
                "Total length of Narmada: 1,312 km; length in Gujarat: 160 km.",
                "Sardar Sarovar Dam is located at Ekta Nagar (Kevadia) in Narmada district."
            ),
            keyTakeawaysGu = listOf(
                "નર્મદાની કુલ લંબાઈ ૧,૩૧૨ કિ.મી. છે, જેમાંથી ગુજરાતમાં ૧૬૦ કિ.મી. વહેણ છે.",
                "નર્મદા જિલ્લાના એકતા નગર (કેવડિયા) ખાતે સરદાર સરોવર બંધ આવેલો છે."
            )
        ),

        // --- GPSC: INDIAN ECONOMY ---
        McqQuestion(
            id = "gpsc_eco_1",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.ECONOMY.id,
            questionTextEn = "Which regulatory authority governs the International Financial Services Centre (IFSC) at GIFT City, Gandhinagar?",
            questionTextGu = "ગાંધીનગરના ગિફ્ટ સિટી (GIFT City) ખાતે આંતરરાષ્ટ્રીય નાણાકીય સેવા કેન્દ્ર (IFSC) નું નિયમન કઈ સત્તામંડળ દ્વારા કરવામાં આવે છે?",
            optionsEn = listOf(
                "IFSCA (International Financial Services Centres Authority)",
                "SEBI Mumbai Regional Office",
                "RBI Ahmedabad",
                "NITI Aayog Financial Wing"
            ),
            optionsGu = listOf(
                "IFSCA (આંતરરાષ્ટ્રીય નાણાકીય સેવા કેન્દ્ર સત્તામંડળ)",
                "સેબી (SEBI) મુંબઈ પ્રાદેશિક કચેરી",
                "આરબીઆઈ (RBI) અમદાવાદ",
                "નીતિ આયોગ નાણાકીય પાંખ"
            ),
            correctOptionIndex = 0,
            explanationEn = "Established under the IFSCA Act, 2019 and headquartered at GIFT City in Gandhinagar, IFSCA is a unified statutory regulator consolidating the powers of RBI, SEBI, IRDAI, and PFRDA for financial products and institutions operating within India's IFSC.",
            explanationGu = "IFSCA અધિનિયમ, ૨૦૧૯ હેઠળ સ્થાપિત અને ગાંધીનગરના ગિફ્ટ સિટી ખાતે વડુંમથક ધરાવતું IFSCA એ RBI, SEBI, IRDAI અને PFRDA ની સત્તાઓને એકીકૃત કરતું સંયુક્ત વૈધાનિક નિયમનકાર છે.",
            referenceNote = "Indian Economy & Gujarat Economic Survey — GIFT IFSC Ecosystem",
            topicTagEn = "Financial Institutions & GIFT IFSC",
            topicTagGu = "નાણાકીય સંસ્થાઓ અને ગિફ્ટ સિટી",
            difficulty = "Prelims GS"
        ),

        // --- GPSC: GENERAL SCIENCE ---
        McqQuestion(
            id = "gpsc_sci_1",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.SCIENCE.id,
            questionTextEn = "What is the primary role of ISRO's Space Applications Centre (SAC) located in Ahmedabad, Gujarat?",
            questionTextGu = "અમદાવાદ, ગુજરાત ખાતે આવેલા ઇસરોના સ્પેસ એપ્લિકેશન્સ સેન્ટર (SAC) ની મુખ્ય ભૂમિકા શું છે?",
            optionsEn = listOf(
                "Design and development of space-borne payloads for communication, navigation, and Earth observation satellites",
                "Launching cryogenic rockets from coastal launchpads",
                "Training astronauts exclusively for deep ocean recovery",
                "Manufacturing solid rocket boosters only"
            ),
            optionsGu = listOf(
                "સંદેશાવ્યવહાર, નેવિગેશન અને પૃથ્વી અવલોકન ઉપગ્રહો માટે પેલોડ્સની ડિઝાઇન અને વિકાસ",
                "દરિયાકાંઠાના લોન્ચપેડ પરથી ક્રાયોજેનિક રોકેટનું પ્રક્ષેપણ",
                "અવકાશયાત્રીઓની સમુદ્રી રિકવરી તાલીમ",
                "માત્ર સોલિડ રોકેટ બૂસ્ટરનું ઉત્પાદન"
            ),
            correctOptionIndex = 0,
            explanationEn = "Space Applications Centre (SAC) in Ahmedabad is one of ISRO's major R&D centers. It designs and develops optical, microwave, and communication payloads for INSAT/GSAT, IRS/EOS Earth Observation satellites, NavIC navigation payloads, and planetary missions like Chandrayaan and Aditya-L1.",
            explanationGu = "અમદાવાદ સ્થિત સ્પેસ એપ્લિકેશન્સ સેન્ટર (SAC) ઇસરોનું પ્રમુખ સંશોધન કેન્દ્ર છે, જે સંદેશાવ્યવહાર (INSAT/GSAT), રિમોટ સેન્સિંગ, NavIC અને ચંદ્રયાન તથા આદિત્ય-L1 જેવા મિશન માટે પેલોડ્સ અને સેન્સર તૈયાર કરે છે.",
            referenceNote = "Science & Technology in Gujarat — ISRO SAC Ahmedabad",
            topicTagEn = "Space Technology & ISRO",
            topicTagGu = "અવકાશ ટેકનોલોજી અને ઇસરો",
            difficulty = "Prelims GS"
        ),

        // --- UPSC: INDIAN POLITY ---
        McqQuestion(
            id = "upsc_pol_1",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.POLITY.id,
            questionTextEn = "Consider the following statements regarding the 'Basic Structure Doctrine' of the Indian Constitution:\n1. It was propounded in the Kesavananda Bharati case (1973).\n2. The Constitution explicitly defines the list of basic features under Article 368.\nWhich of the statements given above is/are correct?",
            questionTextGu = "ભારતીય બંધારણના 'મૂળભૂત માળખાના સિદ્ધાંત' (Basic Structure Doctrine) અંગે નીચેના વિધાનો ધ્યાનમાં લો:\n૧. તે કેશવાનંદ ભારતી કેસ (૧૯૭૩) માં રજૂ કરવામાં આવ્યો હતો.\n૨. બંધારણના અનુચ્છેદ ૩૬૮ હેઠળ મૂળભૂત લક્ષણોની યાદી સ્પષ્ટપણે વ્યાખ્યાયિત કરવામાં આવી છે.\nઉપર આપેલા વિધાનોમાંથી કયું/કયા સાચું/સાચા છે?",
            optionsEn = listOf(
                "1 only",
                "2 only",
                "Both 1 and 2",
                "Neither 1 nor 2"
            ),
            optionsGu = listOf(
                "માત્ર ૧",
                "માત્ર ૨",
                "૧ અને ૨ બંને",
                "૧ કે ૨ એકપણ નહીં"
            ),
            correctOptionIndex = 0,
            explanationEn = "Statement 1 is correct: The Supreme Court laid down the Basic Structure Doctrine in the landmark Kesavananda Bharati vs. State of Kerala (1973) judgment, holding that Parliament's constituent power under Article 368 cannot alter the 'basic structure' of the Constitution. Statement 2 is incorrect: The Constitution nowhere defines 'Basic Structure'; it has been evolved judicially case by case.",
            explanationGu = "વિધાન ૧ સાચું છે: સુપ્રીમ કોર્ટે કેશવાનંદ ભારતી કેસ (૧૯૭૩) માં મૂળભૂત માળખાનો સિદ્ધાંત આપ્યો હતો. વિધાન ૨ ખોટું છે: બંધારણમાં ક્યાંય પણ 'મૂળભૂત માળખા'ની વ્યાખ્યા કે યાદી આપવામાં આવી નથી, તે ન્યાયિક ચુકાદાઓ દ્વારા વિકસિત થઈ છે.",
            referenceNote = "UPSC CSE Prelims GS-1 — Indian Polity (M. Laxmikanth Ch. 11)",
            topicTagEn = "Basic Structure Doctrine",
            topicTagGu = "બંધારણનું મૂળભૂત માળખું",
            difficulty = "Statement-Based",
            keyTakeawaysEn = listOf(
                "Minerva Mills case (1980) held that Judicial Review and balance between Fundamental Rights and DPSPs are part of the Basic Structure.",
                "S.R. Bommai case (1994) affirmed Secularism and Federalism as part of the Basic Structure."
            ),
            keyTakeawaysGu = listOf(
                "મિનર્વા મિલ્સ કેસ (૧૯૮૦) મુજબ ન્યાયિક સમીક્ષા અને મૂળભૂત અધિકારો તથા રાજનીતિના માર્ગદર્શક સિદ્ધાંતો વચ્ચેનું સંતુલન મૂળભૂત માળખાનો ભાગ છે.",
                "એસ.આર. બોમ્મઈ કેસ (૧૯૯૪) માં બિનસાંપ્રદાયિકતા અને સંઘવાદને મૂળભૂત માળખું ગણાવાયું."
            )
        ),
        McqQuestion(
            id = "upsc_pol_2",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.POLITY.id,
            questionTextEn = "With reference to the Tenth Schedule (Anti-Defection Law) of the Constitution of India, consider the following statements:\n1. It was inserted by the 52nd Constitutional Amendment Act, 1985.\n2. The decision of the Presiding Officer on disqualification under the Tenth Schedule is immune from judicial review.\nWhich of the statements given above is/are correct?",
            questionTextGu = "ભારતીય બંધારણની દસમી અનુસૂચિ (પક્ષાંતર વિરોધી કાયદો) અંગે નીચેના વિધાનો ધ્યાનમાં લો:\n૧. તે ૫૨મા બંધારણીય સુધારા અધિનિયમ, ૧૯૮૫ દ્વારા ઉમેરવામાં આવી હતી.\n૨. દસમી અનુસૂચિ હેઠળ ગેરલાયકાત અંગે ગૃહના અધ્યક્ષનો નિર્ણય ન્યાયિક સમીક્ષા (Judicial Review) થી સંપૂર્ણપણે મુક્ત છે.\nઉપર આપેલા વિધાનોમાંથી કયું/કયા સાચું/સાચા છે?",
            optionsEn = listOf(
                "1 only",
                "2 only",
                "Both 1 and 2",
                "Neither 1 nor 2"
            ),
            optionsGu = listOf(
                "માત્ર ૧",
                "માત્ર ૨",
                "૧ અને ૨ બંને",
                "૧ કે ૨ એકપણ નહીં"
            ),
            correctOptionIndex = 0,
            explanationEn = "Statement 1 is correct: The Tenth Schedule was added by the 52nd Amendment Act of 1985 (and later strengthened by the 91st Amendment Act, 2003). Statement 2 is incorrect: In Kihoto Hollohan vs. Zachillhu (1992), the Supreme Court ruled that the Speaker/Chairman acts as a tribunal while deciding defection cases, and their decision is subject to judicial review on grounds of mala fides, perversity, or violation of natural justice.",
            explanationGu = "વિધાન ૧ સાચું છે: ૫૨મા બંધારણીય સુધારા (૧૯૮૫) દ્વારા ૧૦મી અનુસૂચિ ઉમેરવામાં આવી હતી. વિધાન ૨ ખોટું છે: કિહોતો હોલોહાન કેસ (૧૯૯૨) માં સુપ્રીમ કોર્ટે ચુકાદો આપ્યો હતો કે અધ્યક્ષનો નિર્ણય ન્યાયિક સમીક્ષાને આધીન છે.",
            referenceNote = "UPSC CSE Prelims — Parliament & Tenth Schedule (Kihoto Hollohan Case)",
            topicTagEn = "Anti-Defection Law (10th Schedule)",
            topicTagGu = "પક્ષાંતર વિરોધી કાયદો (૧૦મી અનુસૂચિ)",
            difficulty = "Statement-Based"
        ),

        // --- UPSC: GUJARAT HISTORY & CULTURE ---
        McqQuestion(
            id = "upsc_hist_1",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.GUJARAT_HISTORY.id,
            questionTextEn = "With reference to the Gandhian phase of the Indian National Movement, the Ahmedabad Mill Strike (1918) and Kheda Satyagraha (1918) marked key milestones. In which struggle did Mahatma Gandhi undertake his first 'Hunger Strike' in India?",
            questionTextGu = "ભારતીય રાષ્ટ્રીય ચળવળના ગાંધીયુગના સંદર્ભમાં, મહાત્મા ગાંધીએ ભારતમાં તેમની પ્રથમ 'ભૂખ હડતાળ' (ઉપવાસ) કયા આંદોલન દરમિયાન કરી હતી?",
            optionsEn = listOf(
                "Ahmedabad Mill Strike (1918) over the Plague Bonus dispute",
                "Champaran Satyagraha (1917)",
                "Kheda Satyagraha (1918)",
                "Bardoli Satyagraha (1928)"
            ),
            optionsGu = listOf(
                "અમદાવાદ મિલ મજૂર સત્યાગ્રહ (૧૯૧૮) — પ્લેગ બોનસ વિવાદ",
                "ચંપારણ સત્યાગ્રહ (૧૯૧૭)",
                "ખેડા સત્યાગ્રહ (૧૯૧૮)",
                "બારડોલી સત્યાગ્રહ (૧૯૨૮)"
            ),
            correctOptionIndex = 0,
            explanationEn = "In March 1918, Mahatma Gandhi intervened in a dispute between cotton mill owners of Ahmedabad and workers over the discontinuation of the plague bonus, demanding a 35% wage hike. Gandhi undertook his first fast unto death (hunger strike) in India to strengthen the workers' resolve, leading to a tribunal awarding the 35% increase.",
            explanationGu = "માર્ચ ૧૯૧૮માં અમદાવાદના મિલ માલિકો અને કામદારો વચ્ચે પ્લેગ બોનસ અંગેના વિવાદમાં ગાંધીજીએ ૩૫% પગાર વધારાની માંગ સાથે ભારતમાં પ્રથમ વખત ઉપવાસ (ભૂખ હડતાળ) કર્યા હતા.",
            referenceNote = "Spectrum's Modern Indian History — Emergence of Gandhi",
            topicTagEn = "Gandhian Movements in Gujarat",
            topicTagGu = "ગુજરાતમાં ગાંધીયુગના સત્યાગ્રહો",
            difficulty = "High-Yield PYQ"
        ),

        // --- UPSC: GEOGRAPHY ---
        McqQuestion(
            id = "upsc_geo_1",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.GEOGRAPHY.id,
            questionTextEn = "Why does the Gulf of Khambhat and Gulf of Kutch region in Gujarat possess the highest tidal energy potential in India?",
            questionTextGu = "ગુજરાતમાં ખંભાતનો અખાત અને કચ્છનો અખાત શા માટે ભારતમાં સૌથી વધુ ભરતી ઉર્જા (Tidal Energy) ક્ષમતા ધરાવે છે?",
            optionsEn = listOf(
                "Funnel-shaped coastal geomorphology and shallow continental shelf amplifying tidal range up to 8–11 meters",
                "Presence of active submarine volcanic vents warming the coastal water",
                "Direct convergence of the cold Oyashio current with the Arabian Sea",
                "Complete absence of river sediment discharge into the bays"
            ),
            optionsGu = listOf(
                "ગળણી આકારની દરિયાઈ ભૂપૃષ્ઠ રચના અને છીછરો ખંડીય છાજલી વિસ્તાર જે ભરતીની ઊંચાઈ ૮–૧૧ મીટર સુધી વધારે છે",
                "સમુદ્રના તળિયે સક્રિય જ્વાળામુખીની હાજરી",
                "અરબ સાગરમાં ઠંડા મહાસાગરીય પ્રવાહોનું મિલન",
                "નદીઓના કાંપનો સંપૂર્ણ અભાવ"
            ),
            correctOptionIndex = 0,
            explanationEn = "The funnel-shaped estuaries of the Gulf of Khambhat (mean tidal range ~6.8m, peak ~11m) and Gulf of Kutch (mean range ~5.3m, peak ~8m) compress incoming semi-diurnal tidal waves over a broad continental shelf, creating strong tidal currents ideal for tidal barrages and stream turbines.",
            explanationGu = "ખંભાતના અખાત અને કચ્છના અખાતનો ગળણી જેવો આકાર અને વિશાળ ખંડીય છાજલી સમુદ્રી ભરતીના મોજાને સંકુચિત કરે છે, જેથી ભરતીની ઊંચાઈ ૮ થી ૧૧ મીટર સુધી પહોંચે છે.",
            referenceNote = "NCERT Class XI Physical Geography & MNRE Tidal Energy Assessment",
            topicTagEn = "Coastal Geomorphology & Energy",
            topicTagGu = "દરિયાઈ ભૂપૃષ્ઠ અને ભરતી ઉર્જા",
            difficulty = "Conceptual",
            keyTakeawaysEn = listOf(
                "Gujarat has the longest coastline among Indian states (~1,600 km).",
                "Gulf of Khambhat receives major west-flowing rivers: Narmada, Tapi, Mahi, and Sabarmati."
            ),
            keyTakeawaysGu = listOf(
                "ગુજરાત ભારતમાં સૌથી લાંબો દરિયાકિનારો (આશરે ૧,૬૦૦ કિ.મી.) ધરાવતું રાજ્ય છે.",
                "ખંભાતના અખાતમાં નર્મદા, તાપી, મહી અને સાબરમતી જેવી પ્રમુખ નદીઓ મળે છે."
            )
        ),
        McqQuestion(
            id = "upsc_geo_2",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.GEOGRAPHY.id,
            questionTextEn = "Consider the following statements regarding the Black Cotton Soil (Regur Soil) found across the Deccan Trap and Southern/Central Gujarat:\n1. It is derived from weathered basaltic lava rocks and is rich in montmorillonite clay minerals.\n2. It exhibits 'self-ploughing' characteristics by swelling when wet and developing deep cracks during dry seasons.\nWhich of the statements given above is/are correct?",
            questionTextGu = "દક્ષિણ અને મધ્ય ગુજરાત તથા દખ્ખણના ઉચ્ચપ્રદેશમાં જોવા મળતી કાળી કપાસની જમીન (રેગુર જમીન) અંગે નીચેના વિધાનો ધ્યાનમાં લો:\n૧. તે બેસાલ્ટિક લાવા ખડકોના ખવાણથી બનેલી છે અને મોન્ટમોરિલોનાઇટ માટીના ખનિજોથી સમૃદ્ધ છે.\n૨. તે ભીની થાય ત્યારે ફૂલે છે અને સૂકી ઋતુમાં ઊંડી તિરાડો પડે છે, જેનાથી 'સ્વયં-ખેડાણ' (Self-ploughing) જેવી લાક્ષણિકતા ધરાવે છે.\nઉપર આપેલા વિધાનોમાંથી કયું/કયા સાચું/સાચા છે?",
            optionsEn = listOf(
                "Both 1 and 2",
                "1 only",
                "2 only",
                "Neither 1 nor 2"
            ),
            optionsGu = listOf(
                "૧ અને ૨ બંને",
                "માત્ર ૧",
                "માત્ર ૨",
                "૧ કે ૨ એકપણ નહીં"
            ),
            correctOptionIndex = 0,
            explanationEn = "Both statements are correct. Black soils (Vertisols / Regur) are formed from the weathering of Deccan Trap basaltic lava. Due to high clay content (montmorillonite), they retain moisture for a long period, swell when wet, and shrink with wide cracks in summer, enabling natural oxygenation ('self-ploughing'). In Gujarat, Bharuch (Kanam tract) is famous for high-grade cotton cultivation on deep black soil.",
            explanationGu = "બંને વિધાનો સાચા છે. કાળી જમીન (રેગુર) બેસાલ્ટ ખડકોના ખવાણથી બને છે અને ભેજ સંગ્રહ કરવાની ઊંચી ક્ષમતા ધરાવે છે. ગુજરાતમાં ભરૂચ અને વડોદરા વચ્ચેનો 'કાનમ પ્રદેશ' કાળી જમીન અને ઉત્તમ કપાસના ઉત્પાદન માટે જાણીતો છે.",
            referenceNote = "NCERT Class XI India: Physical Environment — Soils of India",
            topicTagEn = "Soils of India & Kanam Tract",
            topicTagGu = "ભારત અને ગુજરાતની જમીનના પ્રકારો",
            difficulty = "Statement-Based"
        ),

        // --- UPSC: INDIAN ECONOMY ---
        McqQuestion(
            id = "upsc_eco_1",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.ECONOMY.id,
            questionTextEn = "If the Reserve Bank of India (RBI) decides to adopt an expansionist monetary policy, which of the following would it NOT do?",
            questionTextGu = "જો ભારતીય રિઝર્વ બેંક (RBI) વિસ્તરણવાદી નાણાકીય નીતિ (Expansionist Monetary Policy) અપનાવવાનું નક્કી કરે, તો તે નીચેનામાંથી શું કરશે નહીં?",
            optionsEn = listOf(
                "Increase the Marginal Standing Facility (MSF) Rate and Repo Rate",
                "Cut and optimize the Statutory Liquidity Ratio (SLR)",
                "Buy government securities in Open Market Operations (OMO)",
                "Reduce the Cash Reserve Ratio (CRR)"
            ),
            optionsGu = listOf(
                "માર્જિનલ સ્ટેન્ડિંગ ફેસિલિટી (MSF) દર અને રેપો રેટમાં વધારો કરવો",
                "વૈધાનિક પ્રવાહિતા ગુણોત્તર (SLR) માં ઘટાડો કરવો",
                "ઓપન માર્કેટ ઓપરેશન્સ (OMO) હેઠળ સરકારી જામીનગીરીઓ ખરીદવી",
                "રોકડ અનામત ગુણોત્તર (CRR) ઘટાડવો"
            ),
            correctOptionIndex = 0,
            explanationEn = "An expansionist monetary policy aims to increase money supply and liquidity in the economy by lowering interest rates (cutting Repo, Reverse Repo, MSF) and lowering reserve requirements (CRR, SLR). Increasing the MSF and Repo Rate makes borrowing costlier and is a contractionary (hawkish) measure.",
            explanationGu = "વિસ્તરણવાદી નાણાકીય નીતિનો હેતુ અર્થતંત્રમાં નાણાંનો પુરવઠો વધારવાનો હોય છે. તેથી RBI રેપો રેટ અને MSF ઘટાડે છે. MSF અને રેપો રેટમાં વધારો કરવો એ સંકોચનવાદી પગલું છે.",
            referenceNote = "UPSC CSE Prelims PYQ & Indian Economy by Ramesh Singh (Monetary Policy)",
            topicTagEn = "Monetary Policy & RBI Tools",
            topicTagGu = "નાણાકીય નીતિ અને RBI ના સાધનો",
            difficulty = "High-Yield PYQ"
        ),

        // --- UPSC: GENERAL SCIENCE ---
        McqQuestion(
            id = "upsc_sci_1",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.SCIENCE.id,
            questionTextEn = "Why is the Lagrange Point 1 (L1) of the Sun-Earth system selected for India's Aditya-L1 solar mission?",
            questionTextGu = "ભારતના આદિત્ય-L1 સૌર મિશન માટે સૂર્ય-પૃથ્વી પ્રણાલીના લાગ્રાન્જ પોઈન્ટ 1 (L1) ની પસંદગી શા માટે કરવામાં આવી છે?",
            optionsEn = listOf(
                "A halo orbit around L1 allows continuous, uninterrupted observation of the Sun without any occultation or eclipses",
                "L1 is located inside the Sun's chromosphere for direct plasma sampling",
                "Gravitational pull of Jupiter shields the spacecraft at L1",
                "Spacecraft at L1 require zero station-keeping fuel for 100 years"
            ),
            optionsGu = listOf(
                "L1 ની આસપાસની હેલો ભ્રમણકક્ષા કોઈપણ ગ્રહણ કે અવરોધ વિના સૂર્યનું સતત નિરીક્ષણ કરવાની સુવિધા આપે છે",
                "L1 સૂર્યના ક્રોમોસ્ફિયરની અંદર આવેલું છે",
                "ગુરુ ગ્રહનું ગુરુત્વાકર્ષણ અવકાશયાનને રક્ષણ આપે છે",
                "ત્યાં ૧૦૦ વર્ષ સુધી બળતણની જરૂર પડતી નથી"
            ),
            correctOptionIndex = 0,
            explanationEn = "Lagrange Point L1 lies about 1.5 million km from Earth towards the Sun (roughly 1% of the Earth-Sun distance). A spacecraft placed in a halo orbit around L1 enjoys an unobstructed view of the Sun 24/7 without Earth's shadow or eclipses, enabling real-time monitoring of solar winds and Coronal Mass Ejections (CMEs).",
            explanationGu = "પૃથ્વીથી સૂર્ય તરફ આશરે ૧૫ લાખ કિ.મી. દૂર આવેલા L1 બિંદુની આસપાસની હેલો ભ્રમણકક્ષામાં મૂકાયેલા ઉપગ્રહને પૃથ્વીના પડછાયા કે ગ્રહણનો અવરોધ નડતો નથી અને સૂર્યનું ૨૪ કલાક સતત અવલોકન થઈ શકે છે.",
            referenceNote = "ISRO Aditya-L1 Mission Dossier & UPSC Science & Tech",
            topicTagEn = "Astrophysics & ISRO Missions",
            topicTagGu = "અવકાશ વિજ્ઞાન અને આદિત્ય-L1",
            difficulty = "Conceptual"
        )
    )

    val mainsPracticePrompts: List<MainsPracticePrompt> = listOf(
        MainsPracticePrompt(
            id = "mains_gpsc_1",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GUJARAT_HISTORY.id,
            marks = 10,
            wordLimit = 150,
            promptEn = "Discuss the salient features of the Maru-Gurjara architectural style developed during the Solanki era in Gujarat with suitable examples.",
            promptGu = "ગુજરાતમાં સોલંકી કાળ દરમિયાન વિકસેલી 'મારુ-ગુર્જર' સ્થાપત્ય શૈલીની મુખ્ય વિશેષતાઓ યોગ્ય ઉદાહરણો સહિત ચર્ચો.",
            sampleStructureHintEn = "Intro: Define Maru-Gurjara synthesis (11th-13th century). Body: Temple plan (Mandovara, Sabha Mandapa, Kirti Torana), stepwells (Rani ki Vav), Sun Temple Modhera, Dilwara/Taranga Jain temples. Conclusion: Legacy in Gujarat's cultural identity.",
            sampleStructureHintGu = "પ્રસ્તાવના: ૧૧-૧૩મી સદીમાં સોલંકી કાળ હેઠળ મારુ-ગુર્જર શૈલીનો ઉદ્ભવ. મુખ્ય ભાગ: મંદિર રચના (ગર્ભગૃહ, સભામંડપ, કીર્તિતોરણ), મોઢેરાનું સૂર્યમંદિર, રાણીની વાવ (પાટણ), વડનગર કીર્તિતોરણ. ઉપસંહાર: યુનેસ્કો વારસો અને સ્થાપત્ય મહત્વ."
        ),
        MainsPracticePrompt(
            id = "mains_gpsc_2",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.ECONOMY.id,
            marks = 10,
            wordLimit = 150,
            promptEn = "Evaluate the strategic role of Dholera Special Investment Region (SIR) and GIFT City in transforming Gujarat into a global semiconductor and fintech hub.",
            promptGu = "ગુજરાતને વૈશ્વિક સેમિકન્ડક્ટર અને ફિનટેક હબમાં રૂપાંતરિત કરવામાં ધોલેરા સ્પેશિયલ ઇન્વેસ્ટમેન્ટ રિજન (SIR) અને ગિફ્ટ સિટી (GIFT City) ની વ્યૂહાત્મક ભૂમિકાનું મૂલ્યાંકન કરો.",
            sampleStructureHintEn = "Intro: Gujarat's industrial shift from manufacturing to knowledge & deep-tech economy. Body: Dholera SIR greenfield smart infrastructure & Semiconductor Policy; GIFT City IFSCA unified regulatory sandbox, green bonds & bullion exchange. Conclusion: Employment & Viksit Gujarat @ 2047 roadmap.",
            sampleStructureHintGu = "પ્રસ્તાવના: ગુજરાતનું પરંપરાગત ઉદ્યોગોથી હાઈ-ટેક અને નાણાકીય સેવાઓ તરફ પ્રયાણ. મુખ્ય ભાગ: ધોલેરા SIR સેમિકન્ડક્ટર ફેબ ઇકોસિસ્ટમ; ગિફ્ટ સિટી IFSCA અને આંતરરાષ્ટ્રીય નાણાકીય કેન્દ્ર. ઉપસંહાર: 'વિકસિત ગુજરાત @ ૨૦૪૭'માં યોગદાન."
        ),
        MainsPracticePrompt(
            id = "mains_upsc_1",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.POLITY.id,
            marks = 15,
            wordLimit = 250,
            promptEn = "\"Cooperative Federalism in India requires a delicate balance between fiscal autonomy of States and national macroeconomic stability.\" Examine in the context of the 16th Finance Commission.",
            promptGu = "\"ભારતમાં સહકારી સંઘવાદ માટે રાજ્યોની નાણાકીય સ્વાયત્તતા અને રાષ્ટ્રીય આર્થિક સ્થિરતા વચ્ચે સંતુલન જરૂરી છે.\" ૧૬મા નાણાપંચના સંદર્ભમાં પરીક્ષણ કરો.",
            sampleStructureHintEn = "Intro: Article 280 & role of Finance Commission as fiscal balancing wheel. Body: Vertical & horizontal devolution challenges, cess/surcharges, demographic performance vs per-capita income gap, GST compensation transition. Conclusion: Transparent formula incentivizing both equity and efficiency.",
            sampleStructureHintGu = "પ્રસ્તાવના: અનુચ્છેદ ૨૮૦ અને નાણાપંચની બંધારણીય ભૂમિકા. મુખ્ય ભાગ: ઊભી અને આડી કર વહેંચણીના મુદ્દા, સેસ અને સરચાર્જ, રાજ્યોની રાજકોષીય સ્વાયત્તતા અને વિકાસશીલ રાજ્યોની જરૂરિયાતો. ઉપસંહાર: સમતા અને કાર્યક્ષમતાનું સંતુલન."
        ),
        MainsPracticePrompt(
            id = "mains_upsc_2",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.GEOGRAPHY.id,
            marks = 10,
            wordLimit = 150,
            promptEn = "Analyze the causes of increasing frequency of cyclones in the Arabian Sea and discuss disaster resilience measures along the Gujarat and western coast.",
            promptGu = "અરબ સાગરમાં ચક્રવાત (વાવાઝોડા) ની વધતી જતી આવૃત્તિના કારણોનું વિશ્લેષણ કરો અને ગુજરાત તથા પશ્ચિમ દરિયાકાંઠે આપત્તિ વ્યવસ્થાપનના પગલાં ચર્ચો.",
            sampleStructureHintEn = "Intro: Rising Arabian Sea surface temperatures (SST > 28°C) and Indian Ocean Dipole. Body: Recent cyclones (Biparjoy, Tauktae), reduced vertical wind shear; Gujarat's zero-casualty evacuation model, mangrove restoration (MISHTI scheme), coastal shelter belts. Conclusion: Climate-resilient coastal infrastructure.",
            sampleStructureHintGu = "પ્રસ્તાવના: અરબ સાગરના સપાટીના તાપમાનમાં વધારો અને આબોહવા પરિવર્તન. મુખ્ય ભાગ: બિપરજોય અને તૌકતે જેવા ચક્રવાતો; ગુજરાતનું આપત્તિ વ્યવસ્થાપન મોડેલ, મેન્ગ્રુવ સંરક્ષણ (MISHTI યોજના) અને અર્લી વોર્નિંગ સિસ્ટમ. ઉપસંહાર: ટકાઉ દરિયાકાંઠાનું માળખું."
        )
    )

    val currentAffairsItems: List<CurrentAffairsItem> = listOf(
        CurrentAffairsItem(
            id = "ca_1",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.ECONOMY.id,
            tagEn = "Gujarat Administration & Economy",
            tagGu = "ગુજરાત વહીવટ અને અર્થતંત્ર",
            dateLabel = "${CurrentAffairsApiService.todayIsoDate(0)} • GS-III Syllabus",
            titleEn = "Gujarat Semiconductor & Electronics Manufacturing Cluster at Dholera SIR",
            titleGu = "ધોલેરા SIR ખાતે ગુજરાત સેમિકન્ડક્ટર અને ઇલેક્ટ્રોનિક્સ મેન્યુફેક્ચરિંગ ક્લસ્ટર",
            summaryEn = "Under the Gujarat Semiconductor Policy, Dholera Special Investment Region ('Semicon City') has operationalized dedicated plug-and-play industrial trunk infrastructure, ultra-pure water supply, and power redundancy for commercial wafer fabrication and OSAT/ATMP units in Sanand and Dholera.",
            summaryGu = "ગુજરાત સેમિકન્ડક્ટર પોલિસી હેઠળ ધોલેરા સ્પેશિયલ ઇન્વેસ્ટમેન્ટ રિજન ('સેમિકોન સિટી') અને સાણંદ ખાતે કોમર્શિયલ વેફર ફેબ્રિકેશન અને OSAT/ATMP એકમો માટે અદ્યતન પ્લગ-એન્ડ-પ્લે માળખું, અલ્ટ્રા-પ્યોર વોટર અને અવિરત વીજ પુરવઠો કાર્યરત કરવામાં આવ્યો છે.",
            syllabusLinkageEn = "GPSC Class 1-2 GS-3: Industrial Policies of Gujarat, SIR Act 2009 & Emerging Technologies",
            syllabusLinkageGu = "GPSC ક્લાસ ૧-૨ GS-3: ગુજરાતની ઔદ્યોગિક નીતિઓ, SIR અધિનિયમ ૨૦૦૯ અને ઉભરતી ટેકનોલોજી",
            practiceMcq = McqQuestion(
                id = "ca_mcq_1",
                examCategory = ExamCategory.GPSC.id,
                subject = ExamSubject.ECONOMY.id,
                questionTextEn = "Which state became the first in India to launch a dedicated 'State Semiconductor Policy' and establish a 'Semicon City' at Dholera SIR?",
                questionTextGu = "ભારતમાં સમર્પિત 'સ્ટેટ સેમિકન્ડક્ટર પોલિસી' જાહેર કરનાર અને ધોલેરા SIR ખાતે 'સેમિકોન સિટી' વિકસાવનાર પ્રથમ રાજ્ય કયું છે?",
                optionsEn = listOf("Gujarat", "Karnataka", "Tamil Nadu", "Maharashtra"),
                optionsGu = listOf("ગુજરાત", "કર્ણાટક", "તમિલનાડુ", "મહારાષ્ટ્ર"),
                correctOptionIndex = 0,
                explanationEn = "Gujarat was the first state in India to announce a dedicated Gujarat Semiconductor Policy (2022–2027) with capital subsidies on top of the India Semiconductor Mission (ISM) incentives.",
                explanationGu = "ભારત સેમિકન્ડક્ટર મિશન (ISM) ઉપરાંત વધારાની મૂડી સબસિડી સાથે સમર્પિત સેમિકન્ડક્ટર પોલિસી જાહેર કરનાર ગુજરાત દેશનું પ્રથમ રાજ્ય છે.",
                referenceNote = "Gujarat Socio-Economic Review & Industrial Policy"
            ),
            isoDate = CurrentAffairsApiService.todayIsoDate(0),
            category = CurrentAffairsCategory.GUJARAT_SPECIAL,
            sourceName = "Gujarat Info & Industries Dept",
            sourceUrl = "https://dst.gujarat.gov.in"
        ),
        CurrentAffairsItem(
            id = "ca_2",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GEOGRAPHY.id,
            tagEn = "Gujarat Ecology & Energy",
            tagGu = "ગુજરાત પર્યાવરણ અને ઉર્જા",
            dateLabel = "${CurrentAffairsApiService.todayIsoDate(0)} • GS-I & GS-III",
            titleEn = "Khavda Hybrid Renewable Energy Park in Kutch Crosses Major Grid Milestone",
            titleGu = "કચ્છના ખાવડા હાઇબ્રિડ રિન્યુએબલ એનર્જી પાર્કે હાંસલ કર્યો નવો ગ્રીડ લક્ષ્યાંક",
            summaryEn = "Located in the Rann of Kutch near Khavda, the world's largest 30 GW hybrid solar-wind renewable energy park is scaling green power transmission corridors and supporting Gujarat's Green Hydrogen Mission.",
            summaryGu = "કચ્છના રણમાં ખાવડા ખાતે આકાર લઈ રહેલો વિશ્વનો સૌથી મોટો ૩૦ ગીગાવોટ (30 GW) ક્ષમતાનો હાઇબ્રિડ સોલાર-વિન્ડ રિન્યુએબલ એનર્જી પાર્ક ગુજરાતના ગ્રીન હાઇડ્રોજન મિશનને નવી ગતિ આપી રહ્યો છે.",
            syllabusLinkageEn = "GPSC & UPSC GS-3: Renewable Energy, Climate Change & Infrastructure",
            syllabusLinkageGu = "GPSC અને UPSC GS-3: પુનઃપ્રાપ્ય ઉર્જા, આબોહવા પરિવર્તન અને માળખાગત સુવિધા",
            practiceMcq = McqQuestion(
                id = "ca_mcq_2",
                examCategory = ExamCategory.GPSC.id,
                subject = ExamSubject.GEOGRAPHY.id,
                questionTextEn = "Where is the world's largest 30 GW Hybrid Renewable Energy Park (Solar + Wind) being developed in Gujarat?",
                questionTextGu = "ગુજરાતમાં વિશ્વનો સૌથી મોટો ૩૦ ગીગાવોટ (30 GW) હાઇબ્રિડ રિન્યુએબલ એનર્જી પાર્ક (સૌર અને પવન ઉર્જા) ક્યાં વિકસાવવામાં આવી રહ્યો છે?",
                optionsEn = listOf("Khavda, Kutch District", "Charanka, Patan District", "Modhera, Mehsana District", "Dhuvaran, Anand District"),
                optionsGu = listOf("ખાવડા, કચ્છ જિલ્લો", "ચારણકા, પાટણ જિલ્લો", "મોઢેરા, મહેસાણા જિલ્લો", "ધુવારણ, આણંદ જિલ્લો"),
                correctOptionIndex = 0,
                explanationEn = "Khavda in Kutch district hosts the 30 GW Hybrid Renewable Energy Park spanning over 72,600 hectares of wasteland in the Great Rann of Kutch.",
                explanationGu = "કચ્છ જિલ્લાના ખાવડા ખાતે ૭૨,૬૦૦ હેક્ટર વિસ્તારમાં ૩૦ ગીગાવોટનો વિશ્વનો સૌથી મોટો હાઇબ્રિડ રિન્યુએબલ એનર્જી પાર્ક વિકસાવવામાં આવી રહ્યો છે.",
                referenceNote = "Gujarat Energy Development Agency (GEDA) & Current Affairs"
            ),
            isoDate = CurrentAffairsApiService.todayIsoDate(0),
            category = CurrentAffairsCategory.ENVIRONMENT_GEO,
            sourceName = "MNRE & GEDA Bulletin",
            sourceUrl = "https://geda.gujarat.gov.in"
        ),
        CurrentAffairsItem(
            id = "ca_3",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.POLITY.id,
            tagEn = "National Governance & Polity",
            tagGu = "રાષ્ટ્રીય શાસન અને રાજવ્યવસ્થા",
            dateLabel = "${CurrentAffairsApiService.todayIsoDate(-1)} • UPSC GS-II",
            titleEn = "Gram Manchitra & e-GramSwaraj Integration for Spatial Planning in Panchayats",
            titleGu = "પંચાયતોમાં અવકાશી આયોજન (Spatial Planning) માટે 'ગ્રામ માનચિત્ર' અને 'ઈ-ગ્રામસ્વરાજ'નું સંકલન",
            summaryEn = "The Ministry of Panchayati Raj has strengthened GIS-based spatial planning through Gram Manchitra integrated with the mActionSoft and e-GramSwaraj portals, empowering Gram Panchayats to prepare data-driven Gram Panchayat Development Plans (GPDP).",
            summaryGu = "પંચાયતી રાજ મંત્રાલયે ગ્રામ પંચાયત વિકાસ યોજના (GPDP) ને વધુ વૈજ્ઞાનિક બનાવવા માટે જીઆઈએસ (GIS) આધારિત 'ગ્રામ માનચિત્ર' એપ્લિકેશનને ઈ-ગ્રામસ્વરાજ પોર્ટલ સાથે જોડી છે.",
            syllabusLinkageEn = "UPSC GS-2: e-Governance, Local Self-Government & Grassroots Devolution",
            syllabusLinkageGu = "UPSC GS-2: ઈ-ગવર્નન્સ, સ્થાનિક સ્વરાજ્યની સંસ્થાઓ અને વિકેન્દ્રીકરણ",
            practiceMcq = McqQuestion(
                id = "ca_mcq_3",
                examCategory = ExamCategory.UPSC.id,
                subject = ExamSubject.POLITY.id,
                questionTextEn = "What is the primary objective of the 'Gram Manchitra' platform launched by the Ministry of Panchayati Raj?",
                questionTextGu = "પંચાયતી રાજ મંત્રાલય દ્વારા શરૂ કરાયેલા 'ગ્રામ માનચિત્ર' પ્લેટફોર્મનો મુખ્ય ઉદ્દેશ્ય શું છે?",
                optionsEn = listOf(
                    "GIS-based spatial planning for Gram Panchayat Development Plans (GPDP)",
                    "Direct cryptocurrency trading for rural cooperative banks",
                    "Issuing diplomatic passports at taluka headquarters",
                    "Replacing State Election Commissions in panchayat polls"
                ),
                optionsGu = listOf(
                    "ગ્રામ પંચાયત વિકાસ યોજના (GPDP) માટે GIS આધારિત ભૌગોલિક આયોજન",
                    "ગ્રામીણ સહકારી બેંકો માટે ક્રિપ્ટોકરન્સી ટ્રેડિંગ",
                    "તાલુકા મથકે પાસપોર્ટ વિતરણ",
                    "રાજ્ય ચૂંટણી પંચનું સ્થાન લેવું"
                ),
                correctOptionIndex = 0,
                explanationEn = "Gram Manchitra is a Geographic Information System (GIS) application created by the Ministry of Panchayati Raj to help Gram Panchayats visualize developmental works across water, health, sanitation, and roads for GPDP.",
                explanationGu = "ગ્રામ માનચિત્ર એ ગ્રામ પંચાયતોને ભૌગોલિક માહિતી પ્રણાલી (GIS) દ્વારા જળ સંચય, આરોગ્ય, શિક્ષણ અને રસ્તાના કામોનું આયોજન કરવામાં મદદ કરતું પ્લેટફોર્મ છે.",
                referenceNote = "PIB & Ministry of Panchayati Raj — e-Governance Initiatives"
            ),
            isoDate = CurrentAffairsApiService.todayIsoDate(-1),
            category = CurrentAffairsCategory.POLITY_GOVERNANCE,
            sourceName = "Press Information Bureau (PIB)",
            sourceUrl = "https://pib.gov.in"
        ),
        CurrentAffairsItem(
            id = "ca_4",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.ECONOMY.id,
            tagEn = "Economy & Financial Regulation",
            tagGu = "અર્થતંત્ર અને નાણાકીય નિયમન",
            dateLabel = "${CurrentAffairsApiService.todayIsoDate(-2)} • GS-III Economy",
            titleEn = "IFSCA Green & Sustainable Finance Framework at GIFT City IFSC",
            titleGu = "ગિફ્ટ સિટી (GIFT IFSC) ખાતે ગ્રીન અને સસ્ટેનેબલ ફાઇનાન્સ માટે IFSCA નું નવું માળખું",
            summaryEn = "The International Financial Services Centres Authority (IFSCA) at GIFT City, Gandhinagar has expanded sovereign green bond listings and transition finance regulations to channel global ESG capital into India's decarbonization and renewable energy projects.",
            summaryGu = "ગાંધીનગરના ગિફ્ટ સિટી ખાતે કાર્યરત IFSCA દ્વારા ભારતના ગ્રીન એનર્જી અને ડીકાર્બોનાઇઝેશન પ્રોજેક્ટ્સમાં વૈશ્વિક મૂડીરોકાણ આકર્ષવા માટે 'સસ્ટેનેબલ એન્ડ ગ્રીન ફાઇનાન્સ' નિયમનકારી માળખું વધુ સુદ્રઢ કરવામાં આવ્યું છે.",
            syllabusLinkageEn = "GPSC & UPSC GS-3: Capital Markets, Green Finance, GIFT IFSC & Sustainable Development",
            syllabusLinkageGu = "GPSC અને UPSC GS-3: મૂડી બજાર, ગ્રીન ફાઇનાન્સ, ગિફ્ટ સિટી અને ટકાઉ વિકાસ",
            practiceMcq = McqQuestion(
                id = "ca_mcq_4",
                examCategory = ExamCategory.GPSC.id,
                subject = ExamSubject.ECONOMY.id,
                questionTextEn = "Which unified statutory regulator governs banking, capital markets, insurance, and fund management at GIFT City IFSC in Gandhinagar?",
                questionTextGu = "ગાંધીનગર સ્થિત ગિફ્ટ સિટી (GIFT IFSC) માં બેંકિંગ, મૂડી બજાર અને વીમા ક્ષેત્રનું સંયુક્ત નિયમન કરતી વૈધાનિક સંસ્થા કઈ છે?",
                optionsEn = listOf(
                    "International Financial Services Centres Authority (IFSCA)",
                    "Forward Markets Commission (FMC)",
                    "Financial Stability and Development Council (FSDC)",
                    "National Payments Corporation of India (NPCI)"
                ),
                optionsGu = listOf(
                    "ઇન્ટરનેશનલ ફાઇનાન્શિયલ સર્વિસીસ સેન્ટર્સ ઓથોરિટી (IFSCA)",
                    "ફોરવર્ડ માર્કેટ્સ કમિશન (FMC)",
                    "ફાઇનાન્શિયલ સ્ટેબિલિટી એન્ડ ડેવલપમેન્ટ કાઉન્સિલ (FSDC)",
                    "નેશનલ પેમેન્ટ્સ કોર્પોરેશન ઓફ ઇન્ડિયા (NPCI)"
                ),
                correctOptionIndex = 0,
                explanationEn = "Established under the IFSCA Act, 2019 with headquarters at GIFT City, Gandhinagar, IFSCA is a unified regulator combining the powers of RBI, SEBI, IRDAI, and PFRDA for International Financial Services Centres in India.",
                explanationGu = "IFSCA અધિનિયમ, ૨૦૧૯ હેઠળ સ્થપાયેલ અને ગાંધીનગર (ગિફ્ટ સિટી) ખાતે વડું મથક ધરાવતી IFSCA દેશના આંતરરાષ્ટ્રીય નાણાકીય સેવા કેન્દ્રો માટે RBI, SEBI, IRDAI અને PFRDA ની સંયુક્ત સત્તા ધરાવતી એકીકૃત નિયમનકારી સંસ્થા છે.",
                referenceNote = "IFSCA Act, 2019 & Economic Survey"
            ),
            isoDate = CurrentAffairsApiService.todayIsoDate(-2),
            category = CurrentAffairsCategory.ECONOMY_POLICY,
            sourceName = "IFSCA & Union Finance Bulletin",
            sourceUrl = "https://ifsca.gov.in"
        ),
        CurrentAffairsItem(
            id = "ca_5",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.SCIENCE.id,
            tagEn = "Space & Defence Tech",
            tagGu = "અવકાશ અને સંરક્ષણ ટેકનોલોજી",
            dateLabel = "${CurrentAffairsApiService.todayIsoDate(-3)} • GS-III Science",
            titleEn = "ISRO Bharatiya Antariksh Station (BAS) & Next-Gen Space Docking Milestones",
            titleGu = "ISRO નું 'ભારતીય અંતરિક્ષ સ્ટેશન' (BAS) અને સ્પેસ ડોકિંગ (SPADEX) ટેકનોલોજી",
            summaryEn = "ISRO is advancing autonomous orbital rendezvous and docking capabilities (SPADEX) alongside human-rated launch vehicle testing for Gaganyaan and the first module of the Bharatiya Antariksh Station (BAS-1).",
            summaryGu = "ઇસરો (ISRO) દ્વારા ગગનયાન મિશન અને ભારતના સ્વદેશી 'ભારતીય અંતરિક્ષ સ્ટેશન' (BAS) ના નિર્માણ માટે અત્યંત મહત્વપૂર્ણ ગણાતી સ્પેસ ડોકિંગ (SPADEX) અને ઓર્બિટલ મોડ્યુલ ટેકનોલોજીનો વિકાસ વેગવાન બનાવવામાં આવ્યો છે.",
            syllabusLinkageEn = "UPSC & GPSC GS-3: Indigenization of Space Technology, ISRO Missions & Robotics",
            syllabusLinkageGu = "UPSC અને GPSC GS-3: અવકાશ ટેકનોલોજીનું સ્વદેશીકરણ અને ISRO મિશન",
            practiceMcq = McqQuestion(
                id = "ca_mcq_5",
                examCategory = ExamCategory.UPSC.id,
                subject = ExamSubject.SCIENCE.id,
                questionTextEn = "What is the primary technological objective of ISRO's 'SPADEX' mission?",
                questionTextGu = "ISRO ના 'SPADEX' મિશનનો મુખ્ય ટેકનોલોજીકલ ઉદ્દેશ્ય શું છે?",
                optionsEn = listOf(
                    "Demonstrating autonomous in-orbit spacecraft rendezvous and docking technology",
                    "Deep-sea polymetallic nodule mining in the Central Indian Ocean",
                    "Sub-surface seismic radar mapping of Jupiter's icy moons",
                    "Replacing optical fiber networks with stratospheric balloons"
                ),
                optionsGu = listOf(
                    "ભ્રમણકક્ષામાં બે અવકાશયાનના સ્વયંસંચાલિત જોડાણ (In-orbit Rendezvous & Docking) ની ટેકનોલોજીનું પ્રદર્શન",
                    "હિંદ મહાસાગરમાં ઊંડા સમુદ્રમાં ખનિજ સંશોધન",
                    "ગુરુ ગ્રહના ઉપગ્રહોનું રડાર મેપિંગ",
                    "સ્ટ્રેટોસ્ફેરિક બલૂન દ્વારા ઇન્ટરનેટ નેટવર્ક"
                ),
                correctOptionIndex = 0,
                explanationEn = "SPADEX (Space Docking Experiment) demonstrates autonomous rendezvous and docking of two spacecraft (Chaser and Target) in low Earth orbit—a prerequisite for lunar sample return and building the Bharatiya Antariksh Station.",
                explanationGu = "SPADEX (Space Docking Experiment) એ પૃથ્વીની નીચલી ભ્રમણકક્ષામાં બે અવકાશયાનને જોડવાની (Docking) સ્વદેશી ટેકનોલોજી છે, જે ભારતીય અંતરિક્ષ સ્ટેશન અને ચંદ્રયાન-૪ માટે અનિવાર્ય છે.",
                referenceNote = "Department of Space & ISRO Annual Report"
            ),
            isoDate = CurrentAffairsApiService.todayIsoDate(-3),
            category = CurrentAffairsCategory.SCIENCE_TECH,
            sourceName = "ISRO Official Dossier",
            sourceUrl = "https://www.isro.gov.in"
        ),
        CurrentAffairsItem(
            id = "ca_6",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GUJARAT_HISTORY.id,
            tagEn = "Cultural Heritage & Archaeology",
            tagGu = "સાંસ્કૃતિક વારસો અને પુરાતત્વ",
            dateLabel = "${CurrentAffairsApiService.todayIsoDate(-5)} • GS-I Heritage",
            titleEn = "Vadnagar Multi-Layered Archaeological Excavation & National Maritime Heritage Complex at Lothal",
            titleGu = "વડનગરનું બહુ-સ્તરીય પુરાતત્વીય ઉત્ખનન અને લોથલ ખાતે નેશનલ મેરિટાઇમ હેરિટેજ કોમ્પ્લેક્સ",
            summaryEn = "Archaeological excavations at Vadnagar (Mehsana) have revealed continuous human habitation spanning over 2,800 years across seven cultural stages, while Lothal is being developed into a world-class National Maritime Heritage Complex (NMHC).",
            summaryGu = "મહેસાણા જિલ્લાના વડનગર ખાતે થયેલા પુરાતત્વીય ઉત્ખનનમાં ૨,૮૦૦ વર્ષથી વધુ સતત માનવ વસવાટ અને સાત સાંસ્કૃતિક તબક્કાઓના પુરાવા મળ્યા છે, જ્યારે લોથલ ખાતે વિશ્વકક્ષાનું 'નેશનલ મેરિટાઇમ હેરિટેજ કોમ્પ્લેક્સ' (NMHC) નિર્માણાધીન છે.",
            syllabusLinkageEn = "GPSC & UPSC GS-1: Cultural Heritage of Gujarat, Indus Valley & Ancient Trade Routes",
            syllabusLinkageGu = "GPSC અને UPSC GS-1: ગુજરાતનો સાંસ્કૃતિક વારસો, સિંધુ ખીણ અને પ્રાચીન વેપાર માર્ગો",
            practiceMcq = McqQuestion(
                id = "ca_mcq_6",
                examCategory = ExamCategory.GPSC.id,
                subject = ExamSubject.GUJARAT_HISTORY.id,
                questionTextEn = "At which ancient Harappan port site in Gujarat is the Ministry of Ports, Shipping and Waterways developing the 'National Maritime Heritage Complex' (NMHC)?",
                questionTextGu = "ગુજરાતના કયા પ્રાચીન હડપ્પીય બંદર સ્થળે 'નેશનલ મેરિટાઇમ હેરિટેજ કોમ્પ્લેક્સ' (NMHC) વિકસાવવામાં આવી રહ્યું છે?",
                optionsEn = listOf(
                    "Lothal (Ahmedabad District)",
                    "Surkotada (Kutch District)",
                    "Kuntasi (Morbi District)",
                    "Rojdi (Rajkot District)"
                ),
                optionsGu = listOf(
                    "લોથલ (અમદાવાદ જિલ્લો)",
                    "સુરકોટડા (કચ્છ જિલ્લો)",
                    "કુંતાસી (મોરબી જિલ્લો)",
                    "રોજડી (રાજકોટ જિલ્લો)"
                ),
                correctOptionIndex = 0,
                explanationEn = "The National Maritime Heritage Complex (NMHC) is being developed at Lothal in Ahmedabad district (Bhal region) near the Bhogavo river, celebrating India's 4,500-year-old maritime heritage and the world's oldest artificial brick dockyard.",
                explanationGu = "અમદાવાદ જિલ્લાના ભાલ પ્રદેશમાં ભોગાવો નદીના કાંઠે આવેલા પ્રાચીન હડપ્પીય બંદર 'લોથલ' ખાતે ભારતના ૪,૫૦૦ વર્ષ જૂના દરિયાઈ વારસાને ઉજાગર કરતું નેશનલ મેરિટાઇમ હેરિટેજ કોમ્પ્લેક્સ તૈયાર થઈ રહ્યું છે.",
                referenceNote = "Ministry of Culture & Gujarat State Archaeology"
            ),
            isoDate = CurrentAffairsApiService.todayIsoDate(-5),
            category = CurrentAffairsCategory.HISTORY_CULTURE,
            sourceName = "ASI & Ministry of Culture",
            sourceUrl = "https://asi.nic.in"
        )
    )

    val syllabusTopics: List<SyllabusTopicTemplate> = listOf(
        SyllabusTopicTemplate(
            topicKey = "POLITY_CONST_FRAMEWORK",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.POLITY.id,
            titleEn = "Preamble, Fundamental Rights, DPSP & Fundamental Duties",
            titleGu = "આમુખ, મૂળભૂત અધિકારો, રાજનીતિના માર્ગદર્શક સિદ્ધાંતો અને મૂળભૂત ફરજો",
            subtopicsEn = "Articles 12–51A, Writ Jurisdiction, Kesavananda Bharati & Minerva Mills doctrines",
            subtopicsGu = "અનુચ્છેદ ૧૨ થી ૫૧A, રીટ સત્તાક્ષેત્ર અને બંધારણીય સુધારા"
        ),
        SyllabusTopicTemplate(
            topicKey = "POLITY_PANCHAYATI_RAJ",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.POLITY.id,
            titleEn = "73rd & 74th Amendments & Gujarat Panchayats Act, 1993",
            titleGu = "૭૩મો અને ૭૪મો બંધારણીય સુધારો અને ગુજરાત પંચાયત અધિનિયમ, ૧૯૯૩",
            subtopicsEn = "Gram Sabha, Samras Gram Yojana, PESA Act in Scheduled Areas of Gujarat",
            subtopicsGu = "ગ્રામસભા, સમરસ ગ્રામ યોજના અને ગુજરાતના અનુસૂચિત વિસ્તારોમાં PESA કાયદો"
        ),
        SyllabusTopicTemplate(
            topicKey = "HIST_INDUS_SOLANKI",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GUJARAT_HISTORY.id,
            titleEn = "Indus Valley Sites in Gujarat, Maitraka & Solanki Dynasties",
            titleGu = "ગુજરાતમાં સિંધુ ખીણના સ્થળો, મૈત્રક અને સોલંકી વંશનો સુવર્ણકાળ",
            subtopicsEn = "Lothal, Dholavira, Rangpur, Vallabhi University, Patan Architecture & Literature",
            subtopicsGu = "લોથલ, ધોળાવીરા, રંગપુર, વલભી વિદ્યાપીઠ, પાટણનું સ્થાપત્ય અને સાહિત્ય"
        ),
        SyllabusTopicTemplate(
            topicKey = "HIST_TRIBAL_HERITAGE",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GUJARAT_HISTORY.id,
            titleEn = "Folk Culture, Fairs, Bhavai, Garba & Tribal Arts of Gujarat",
            titleGu = "ગુજરાતની લોકસંસ્કૃતિ, મેળાઓ, ભવાઈ, ગરબા અને આદિવાસી કલા-વારસો",
            subtopicsEn = "Tarnetar & Bhavnath fairs, Pithora paintings, Patola of Patan, Rogan Art of Kutch",
            subtopicsGu = "તરણેતર અને ભવનાથનો મેળો, પીઠોરા ચિત્રકલા, પાટણના પટોળા અને કચ્છની રોગાન કલા"
        ),
        SyllabusTopicTemplate(
            topicKey = "GEO_GUJARAT_DRAINAGE",
            examCategory = ExamCategory.GPSC.id,
            subject = ExamSubject.GEOGRAPHY.id,
            titleEn = "Physiography, River Systems, Soils & Agro-Climatic Zones of Gujarat",
            titleGu = "ગુજરાતનું ભૂપૃષ્ઠ, નદી તંત્ર, જમીનના પ્રકારો અને કૃષિ-આબોહવા ઝોન",
            subtopicsEn = "Kutch, Saurashtra radial drainage, Narmada-Sabarmati-Mahi-Tapi basins, Coastal wetlands",
            subtopicsGu = "કચ્છ, સૌરાષ્ટ્રનું ત્રિજ્યાકાર નદી તંત્ર, નર્મદા-સાબરમતી-મહી-તાપી બેસિન"
        ),
        SyllabusTopicTemplate(
            topicKey = "ECO_FISCAL_GIFT",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.ECONOMY.id,
            titleEn = "Monetary Policy, Fiscal Federalism, Inclusive Growth & IFSC Ecosystem",
            titleGu = "નાણાકીય નીતિ, રાજકોષીય સંઘવાદ, સમાવેશી વિકાસ અને IFSC ઇકોસિસ્ટમ",
            subtopicsEn = "RBI MPC tools, FRBM Act, Union & Gujarat Budget analysis, Digital Public Infrastructure",
            subtopicsGu = "RBI નાણાકીય નીતિ, બજેટ વિશ્લેષણ અને ડિજિટલ પબ્લિક ઇન્ફ્રાસ્ટ્રક્ચર (DPI)"
        ),
        SyllabusTopicTemplate(
            topicKey = "SCI_ISRO_BIOTECH",
            examCategory = ExamCategory.UPSC.id,
            subject = ExamSubject.SCIENCE.id,
            titleEn = "Space Technology (ISRO Missions), Biotechnology, AI & Clean Energy",
            titleGu = "અવકાશ ટેકનોલોજી (ISRO મિશન), બાયોટેકનોલોજી, AI અને ગ્રીન એનર્જી",
            subtopicsEn = "Gaganyaan, Aditya-L1, Chandrayaan-3, CRISPR Gene Editing, National Green Hydrogen Mission",
            subtopicsGu = "ગગનયાન, આદિત્ય-L1, ચંદ્રયાન-૩, જીન એડિટિંગ અને નેશનલ ગ્રીન હાઇડ્રોજન મિશન"
        )
    )
}

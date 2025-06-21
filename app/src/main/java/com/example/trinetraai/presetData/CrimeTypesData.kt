package com.example.trinetraai.presetData

object CrimeTypesData {

    val crimeTypeMap: Map<String, List<String>> = mapOf(

        "General Crimes" to listOf(
            "Theft", "Assault", "Burglary", "Robbery", "Murder",
            "Drug Possession", "Vandalism", "Fraud", "Kidnapping",
            "Dowry Death", "Attempt to Murder", "Domestic Violence",
            "Child Abuse", "Human Trafficking", "Rioting", "Bribery",
            "Extortion", "Acid Attack", "Drunk Driving", "Arson",
            "Cheating", "Stalking", "Money Laundering", "Obscenity",
            "Attempt to Suicide", "Grievous Hurt", "Criminal Intimidation",
            "Abduction", "Culpable Homicide Not Amounting to Murder",
            "Voluntarily Causing Hurt", "Wrongful Restraint",
            "Wrongful Confinement", "Criminal Force", "Mischief",
            "Criminal Trespass", "Housebreaking", "Dacoity",
            "Criminal Breach of Trust", "Receiving Stolen Property"
        ),

        "Cyber Crimes" to listOf(
            "Cyber Crime", "Online Harassment", "Cyber Stalking",
            "Cyber Espionage", "Cyber Terrorism", "Online Impersonation (non-harassment related)",
            "Phishing (General financial fraud)", "DDoS Attack"
        ),

        "Women & Child Related Crimes" to listOf(
            "Rape", "Sexual Harassment", "Honour Killing", "Female Infanticide",
            "Child Marriage", "Workplace Harassment", "Voyeurism", "Public Indecency",
            "Child Abuse", "Dowry Death", "Domestic Violence", "Child Labour (Violation of laws)",
            "Bonded Labour"
        ),

        "White Collar Crimes" to listOf(
            "Embezzlement", "Counterfeiting", "Identity Theft", "Tax Evasion",
            "Insider Trading", "Corruption (Broader than just bribery)", "Forgery",
            "Falsification of Accounts", "Illegal Gambling", "Accepting Illegal Gratuities",
            "Securities Fraud", "Insurance Fraud", "Bank Fraud", "Credit Card Fraud",
            "Ponzi Scheme", "Supply Chain Fraud", "Criminal Breach of Contract"
        ),

        "Environmental & Wildlife" to listOf(
            "Environmental Crime", "Violation of Environmental Laws",
            "Wildlife Poaching", "Cruelty to Animals", "Illegal Mining",
            "Illegal Sand Dredging"
        ),

        "Terrorism & State Crimes" to listOf(
            "Terrorism", "Waging War Against the State", "Incitement to Riot",
            "Sedition", "Contempt of Court", "Criminal Conspiracy"
        ),

        "Smuggling & Trafficking" to listOf(
            "Drug Trafficking", "Arms Trafficking", "Smuggling",
            "Human Trafficking", "Smuggling of Antiquities",
            "Human Organ Trafficking"
        ),

        "Police & Legal Violations" to listOf(
            "Perjury", "Filing False Information to Police",
            "Disobedience to Order Duly Promulgated by Public Servant",
            "Obstruction of Justice", "Witness Tampering",
            "Resisting Arrest", "Escaping Lawful Custody", "Harbouring an Offender"
        ),

        "Other Notable Crimes" to listOf(
            "Black Marketing", "Adulteration of Food or Drugs",
            "Medical Negligence (Criminal)", "Illegal Construction",
            "Violation of Building Codes", "Selling Adulterated Products",
            "Exploitation of Labour", "Endangering Life or Personal Safety of Others",
            "Spreading Disease Malignantly", "Illegal Possession of Weapons",
            "Explosives Act Violations", "Bigamy", "Defamation",
            "Piracy (Copyright/Intellectual Property)", "Organized Crime",
            "Mugging", "ShopLifting", "Vagrancy (where criminalized)",
            "Carjacking", "Assault on Public Servant", "Illegal Manufacturing of Drugs",
            "Possession of Illegal Firearms", "Passport Fraud", "Visa Fraud"
        )
    )
}

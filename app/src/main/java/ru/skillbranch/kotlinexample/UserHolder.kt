package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName:String,
        email:String,
        password:String
    ):User{
        val user = User.makeUser(fullName, email=email, password=password)
        //проверка на существующий email
        return if (map[email] == null) {
            user.also{map[it.login] = it}
        }
        else{
            throw IllegalArgumentException("A user with this email already exists")
        }
        /*return User.makeUser(fullName, email=email, password=password)
            .also{user -> map[user.login] = user}*/
    }

    fun registerUserByPhone(fullName: String, rawPhone: String) : User {
        val user = User.makeUser(fullName, rawPhone)
        //проверка на валидный телефон
        val isFirstPlus = rawPhone.first() == '+'  //первый плюс
        val is11digits = rawPhone.replace("""^\d""".toRegex(), "").length == 11 //содержит 11 цифр
        val containsLetters = rawPhone.contains("""\w""".toRegex())
        return if (isFirstPlus and is11digits and !containsLetters){
            //проверка на существующий телефон
            if(map[rawPhone] == null){
                user.also { map[it.login] = it }
            }
            else{
                throw IllegalArgumentException("A user with this phone already exists")
            }
        }
        else {
            throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
        }
        //проверка на существующий телефон
    }

    fun loginUser(login:String, password:String):String?{
        return map[login.trim()]?.run{
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun requestAccessCode(login:String): Unit{
        val user = map[login]
        val newCode = user?.generateAccessCode()
        user?.accessCode = newCode
        if (newCode != null) {
            user?.changeAccessCode(newCode)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }
}
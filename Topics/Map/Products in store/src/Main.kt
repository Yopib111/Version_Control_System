fun bill(priceList: Map<String, Int>, shoppingList: MutableList<String>): Int {
    var result = 0
    for (i in shoppingList) {
        result += priceList[i]?:0
    }
    // put your code here
    return result
}
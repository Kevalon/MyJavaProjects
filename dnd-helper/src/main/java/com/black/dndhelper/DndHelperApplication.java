package com.black.dndhelper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DndHelperApplication {


    /*
     * v2 todo:
     *     1. Будет вывод конкретной погоды под текущий период, чтобы я конкретно видел, какая сейчас погода
     *     2. Добавить авторолл случайного события при смене периода
     *     3. Добавить динамическое изменение шанса случайного события с чекбоксами, которые будут менять шанс, согласно правилам.
     *     4. Подумать и добавить смену пресетов для разных сессий (через проставление профиля, например): разная погода, в основном. (разные дао?)
     */
    public static void main(String[] args) {
        SpringApplication.run(DndHelperApplication.class, args);
    }

}

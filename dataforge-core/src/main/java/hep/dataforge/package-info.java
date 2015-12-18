/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge;

/**
 * Пакет является корневым для всей структуры. Основная идея структуры сводится к использованию менеджеров.
 * В идеале, менеджер является единым способом общения пакета с внешним миром.
 * Менеджер (любой кроме GlobalContext) является динамическим объектом, то есть может быть инстанцирован несколько раз 
 * (удобно в плане многопоточности, кстати в этом же плане все менеджеры должны быть сделаны конкурентными). 
 * Имеется один статический GlobalContext, содержащий один или несколько экземпляров каждого менеджера. 
 * Хотя неявным образом он всегда присутсвует (сожержит информацию об опциях), его явное использование
 * не является обязательным, то есть пользовательская программа может работать напрямую 
 * с менеджерами пакетов.
 * 
 */

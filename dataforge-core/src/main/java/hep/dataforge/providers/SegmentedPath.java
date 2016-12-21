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
package hep.dataforge.providers;

import hep.dataforge.exceptions.PathSyntaxException;
import hep.dataforge.names.Name;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Путь в формате target1::path1/target2::path2. Блоки между / называются
 * сегментами.
 *
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
class SegmentedPath implements Path {


    private final LinkedList<PathSegment> segments;
    /**
     * Для наследования цели
     */
    private final String target;

    SegmentedPath(List<PathSegment> segments, String target) {
        this.segments = new LinkedList(segments);
        String newTarget = this.segments.getFirst().target();

        if (newTarget.isEmpty()) {
            //Если новый заголовок без цели, то цель наследуется
            this.target = target;
        } else {
            //Если цель задекларирована, то используется она
            this.target = newTarget;
        }

    }

    protected SegmentedPath(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) {
            throw new IllegalArgumentException("Empty argument in the path constructor");
        }
        String[] split = normalize(pathStr).split(PATH_SEGMENT_SEPARATOR);
        segments = new LinkedList<>();

        if (split.length == 0) {
            throw new PathSyntaxException();
        }

        for (String segmentStr : split) {
            segments.add(new PathSegment(segmentStr));
        }

        target = segments.get(0).target();
    }

    /**
     * {@inheritDoc}
     */
    public static SegmentedPath of(String pathStr) {
        return new SegmentedPath(pathStr);
    }

    /**
     * Устраняет ведущиие и конечные "/"
     *
     * @param path
     * @return
     */
    private static String normalize(String path) {
        String res = path.trim();
        // убираем ведущие сепараторы
        while (res.startsWith(PATH_SEGMENT_SEPARATOR)) {
            res = res.substring(1);
        }
        while (res.endsWith(PATH_SEGMENT_SEPARATOR)) {
            res = res.substring(0, res.length() - 1);
        }
        return res;

    }

    /** {@inheritDoc} */
    @Override
    public String target() {
        if (target == null) {
            return TARGET_EMPTY;
        }
        return target;
    }

    /** {@inheritDoc} */
    @Override
    public Name name() {
        return segments.getFirst().name();
    }

    /**
     * <p>head.</p>
     *
     * @return a {@link hep.dataforge.providers.PathSegment} object.
     */
    public PathSegment head() {
        return this.segments.peekFirst();
    }

    /**
     * <p>size.</p>
     *
     * @return a int.
     */
    public int size() {
        return this.segments.size();
    }

    /**
     * {@inheritDoc}
     *
     * Возвращает путь за исключением первого сегмента
     */
    @Override
    public SegmentedPath tail() {
        if (segments.size() <= 1) {
            return null;
        }
        List<PathSegment> newSegments = segments.subList(1, segments.size());
        return new SegmentedPath(newSegments, target);
    }

    /**
     * {@inheritDoc}
     *
     * Является ли этот путь односегментным(конечным)
     */
    @Override
    public boolean hasTail() {
        return size() > 1;
    }

    /**
     * Глобальная цель для последнего сегмента пути. Соответствует типу
     * возвращаемого объекта
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFinalTarget() {
        // Идем по сегментам в обратном порядке и ищем первый раз, когда появляется объявленная цель
        for (Iterator<PathSegment> it = segments.descendingIterator(); it.hasNext();) {
            Path segment = it.next();
            if (!segment.target().equals(TARGET_EMPTY)) {
                return segment.target();
            }
        }
        //Если цель не объявлена ни в одном из сегментов, возвращаем пустую цель
        return TARGET_EMPTY;
    }

    @Override
    public Path setTarget(String target) {
        return new SegmentedPath(segments, target);
    }

    
}

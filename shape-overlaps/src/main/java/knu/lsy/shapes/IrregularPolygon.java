package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class IrregularPolygon extends Shape {
    private List<Point> vertices;

    public IrregularPolygon(Point center, double radius, int numVertices) {
        super(center, radius);
        this.vertices = generateIrregularVertices(numVertices);
    }

    private List<Point> generateIrregularVertices(int numVertices) {
        List<Point> points = new ArrayList<>();

        // 1. 무작위 각도로 점들 생성
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            angles.add(Math.random() * 2 * Math.PI);
        }
        Collections.sort(angles); // 각도 순으로 정렬

        // 2. 각 점에 대해 무작위 반경 적용
        for (int i = 0; i < numVertices; i++) {
            double angle = angles.get(i);
            double r = radius * (0.5 + Math.random() * 0.5);
            double x = center.getX() + r * Math.cos(angle);
            double y = center.getY() + r * Math.sin(angle);
            points.add(new Point(x, y));
        }

        // 간단한 컨벡스 헐 생성 (선분 교차 방지)
        return createSimpleConvexHull(points);
    }

    private List<Point> createSimpleConvexHull(List<Point> points) {
        // 간단한 컨벡스 헐 구현
        if (points.size() <= 3) return points;

        // x 좌표로 정렬
        points.sort(Comparator.comparingDouble(Point::getX));

        List<Point> hull = new ArrayList<>();

        // 하부 헐
        for (Point p : points) {
            while (hull.size() >= 2 && orientation(hull.get(hull.size() - 2),
                    hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        // 상부 헐
        int lowerSize = hull.size();
        for (int i = points.size() - 2; i >= 0; i--) {
            Point p = points.get(i);
            while (hull.size() > lowerSize && orientation(hull.get(hull.size() - 2),
                    hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        // 마지막 점 제거 (중복)
        if (hull.size() > 1) hull.remove(hull.size() - 1);

        return hull;
    }

    private double orientation(Point p, Point q, Point r) {
        return (q.getX() - p.getX()) * (r.getY() - p.getY()) -
                (q.getY() - p.getY()) * (r.getX() - p.getX());
    }

    // TODO: 학생 과제 - 일반 다각형의 겹침 감지 알고리즘 구현
    @Override
    public boolean overlaps(Shape other) {
        if (other instanceof IrregularPolygon || other instanceof RegularPolygon) {
            return checkPolygonOverlap(this.getVertices(), other.getVertices());
        } else if (other instanceof Circle) {
            return checkCirclePolygonOverlap((Circle) other, this);
        }
        return false;
    }

    // SAT 알고리즘
    private boolean checkPolygonOverlap(List<Point> a, List<Point> b) {
        List<Point> allPoints = new java.util.ArrayList<>();
        allPoints.addAll(a);
        allPoints.addAll(b);

        for (int i = 0; i < a.size(); i++) {
            Point p1 = a.get(i);
            Point p2 = a.get((i + 1) % a.size());

            double axisX = -(p2.getY() - p1.getY());
            double axisY = p2.getX() - p1.getX();

            if (!isOverlappingOnAxis(axisX, axisY, a, b)) return false;
        }

        for (int i = 0; i < b.size(); i++) {
            Point p1 = b.get(i);
            Point p2 = b.get((i + 1) % b.size());

            double axisX = -(p2.getY() - p1.getY());
            double axisY = p2.getX() - p1.getX();

            if (!isOverlappingOnAxis(axisX, axisY, a, b)) return false;
        }

        return true;
    }

    // 투영 후 겹침 검사
    private boolean isOverlappingOnAxis(double axisX, double axisY, List<Point> a, List<Point> b) {
        double[] aProj = projectVertices(a, axisX, axisY);
        double[] bProj = projectVertices(b, axisX, axisY);

        return !(aProj[1] < bProj[0] || bProj[1] < aProj[0]);
    }

    // 벡터 투영 범위 계산
    private double[] projectVertices(List<Point> points, double axisX, double axisY) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (Point p : points) {
            double proj = (p.getX() * axisX + p.getY() * axisY) / Math.sqrt(axisX * axisX + axisY * axisY);
            min = Math.min(min, proj);
            max = Math.max(max, proj);
        }

        return new double[]{min, max};
    }

    // 원 vs 다각형
    private boolean checkCirclePolygonOverlap(Circle circle, IrregularPolygon polygon) {
        Point center = circle.getCenter();
        double radius = circle.getRadius();
        List<Point> vertices = polygon.getVertices();

        // 1. 원 중심이 다각형 내부에 있는가?
        if (isPointInsidePolygon(center, vertices)) return true;

        // 2. 원과 다각형의 변이 교차하는가?
        for (int i = 0; i < vertices.size(); i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % vertices.size());
            if (circleLineIntersect(center, radius, p1, p2)) return true;
        }

        return false;
    }

    // 점이 다각형 안에 있는지 (Ray-casting algorithm)
    private boolean isPointInsidePolygon(Point point, List<Point> vertices) {
        int count = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Point a = vertices.get(i);
            Point b = vertices.get((i + 1) % vertices.size());
            if (rayIntersectsSegment(point, a, b)) count++;
        }
        return count % 2 == 1;
    }

    private boolean rayIntersectsSegment(Point p, Point a, Point b) {
        if (a.getY() > b.getY()) {
            Point temp = a;
            a = b;
            b = temp;
        }
        if (p.getY() == a.getY() || p.getY() == b.getY())
            p = new Point(p.getX(), p.getY() + 0.0001);

        if (p.getY() < a.getY() || p.getY() > b.getY()) return false;
        if (p.getX() >= Math.max(a.getX(), b.getX())) return false;

        if (p.getX() < Math.min(a.getX(), b.getX())) return true;

        double m = (b.getY() - a.getY()) / (b.getX() - a.getX());
        double xInt = a.getX() + (p.getY() - a.getY()) / m;
        return p.getX() < xInt;
    }

    // 선분과 원이 교차하는지 검사
    private boolean circleLineIntersect(Point c, double r, Point a, Point b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();

        double fx = a.getX() - c.getX();
        double fy = a.getY() - c.getY();

        double aCoef = dx * dx + dy * dy;
        double bCoef = 2 * (fx * dx + fy * dy);
        double cCoef = fx * fx + fy * fy - r * r;

        double discriminant = bCoef * bCoef - 4 * aCoef * cCoef;
        if (discriminant < 0) return false;

        discriminant = Math.sqrt(discriminant);
        double t1 = (-bCoef - discriminant) / (2 * aCoef);
        double t2 = (-bCoef + discriminant) / (2 * aCoef);

        return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "irregularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);

        JSONArray verticesArray = new JSONArray();
        for (Point vertex : vertices) {
            verticesArray.put(vertex.toJSON());
        }
        json.put("vertices", verticesArray);

        return json;
    }

    @Override
    public String getShapeType() {
        return "irregularPolygon";
    }

    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}

package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class Circle extends Shape {

    public Circle(Point center, double radius) {
        super(center, radius);
    }

    // TODO: 학생 과제 - 원의 겹침 감지 알고리즘 구현
    @Override
    public boolean overlaps(Shape other) {
        if (other instanceof Circle) {
            // 원 - 원 겹침 검사
            Circle c = (Circle) other;
            double dist = this.center.distanceTo(c.getCenter());
            return dist < (this.radius + c.getRadius());

        } else {
            // 원 - 다각형 겹침 검사
            List<Point> vertices = other.getVertices();

            // 1. 다각형의 어떤 정점이라도 원 안에 있는지 확인
            for (Point p : vertices) {
                if (center.distanceTo(p) <= radius) return true;
            }

            // 2. 다각형의 어떤 변이라도 원과 교차하는지 확인
            for (int i = 0; i < vertices.size(); i++) {
                Point a = vertices.get(i);
                Point b = vertices.get((i + 1) % vertices.size());
                if (circleLineIntersect(this.center, this.radius, a, b)) return true;
            }
        }

        return false; // 위 조건에 걸리지 않으면 겹치지 않음
    }

    // 선분과 원의 교차 여부 판정
    private boolean circleLineIntersect(Point c, double r, Point a, Point b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();

        double fx = a.getX() - c.getX();
        double fy = a.getY() - c.getY();

        double aCoef = dx * dx + dy * dy;
        double bCoef = 2 * (fx * dx + fy * dy);
        double cCoef = fx * fx + fy * fy - r * r;

        double discriminant = bCoef * bCoef - 4 * aCoef * cCoef;
        if (discriminant < 0) return false; // 교차하지 않음

        discriminant = Math.sqrt(discriminant);
        double t1 = (-bCoef - discriminant) / (2 * aCoef);
        double t2 = (-bCoef + discriminant) / (2 * aCoef);

        return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "circle");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);
        return json;
    }

    @Override
    public String getShapeType() {
        return "circle";
    }

    @Override
    public List<Point> getVertices() {
        // 원의 경계를 근사하는 점들 생성
        List<Point> vertices = new ArrayList<>();
        int numPoints = 32;
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            vertices.add(new Point(x, y));
        }
        return vertices;
    }
}

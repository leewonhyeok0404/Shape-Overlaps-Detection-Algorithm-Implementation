package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class RegularPolygon extends Shape {
    private int sides;
    private double rotationAngle;
    private List<Point> vertices;

    public RegularPolygon(Point center, double radius, int sides, double rotationAngle) {
        super(center, radius);
        this.sides = sides;
        this.rotationAngle = rotationAngle;
        this.vertices = generateVertices();
    }

    private List<Point> generateVertices() {
        List<Point> points = new ArrayList<>();
        double angleStep = 2 * Math.PI / sides;

        for (int i = 0; i < sides; i++) {
            double angle = angleStep * i + rotationAngle;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            points.add(new Point(x, y));
        }

        return points;
    }

    // TODO: 학생 과제 - 정다각형의 겹침 감지 알고리즘 구현
    @Override
    public boolean overlaps(Shape other) {
        List<Point> vertices1 = this.getVertices();
        List<Point> vertices2 = other.getVertices();

        List<Point> axes = getAxes(vertices1);
        axes.addAll(getAxes(vertices2)); // 둘 다각형의 축 사용

        for (Point axis : axes) {
            double[] projection1 = projectPolygonOntoAxis(vertices1, axis);
            double[] projection2 = projectPolygonOntoAxis(vertices2, axis);

            if (!overlapsOnAxis(projection1, projection2)) {
                return false; // 분리 축이 존재하면 겹치지 않음
            }
        }

        return true; // 모든 축에서 겹치면 겹침
    }

    private List<Point> getAxes(List<Point> vertices) {
        List<Point> axes = new ArrayList<>();
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % n);

            double dx = p2.getX() - p1.getX();
            double dy = p2.getY() - p1.getY();

            // 법선 벡터 = (-dy, dx)
            Point normal = new Point(-dy, dx);
            axes.add(normalize(normal));
        }

        return axes;
    }

    private Point normalize(Point v) {
        double length = Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY());
        return new Point(v.getX() / length, v.getY() / length);
    }

    // 도형 정점들을 특정 축에 투영해 최소/최대 범위를 구함
    private double[] projectPolygonOntoAxis(List<Point> vertices, Point axis) {
        double min = dot(vertices.get(0), axis);
        double max = min;

        for (Point p : vertices) {
            double projection = dot(p, axis);
            if (projection < min) min = projection;
            if (projection > max) max = projection;
        }

        return new double[]{min, max};
    }

    private double dot(Point a, Point b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    // 두 투영 범위가 겹치는지 판단
    private boolean overlapsOnAxis(double[] proj1, double[] proj2) {
        return !(proj1[1] < proj2[0] || proj2[1] < proj1[0]);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "regularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("sides", sides);
        json.put("rotationAngle", rotationAngle);
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
        return "regularPolygon";
    }

    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}
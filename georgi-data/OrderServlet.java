public class OrderServlet extends HttpServlet {
    private final OrderService service = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int userId = Integer.parseInt(req.getParameter("userId"));
        int productId = Integer.parseInt(req.getParameter("productId"));
        int quantity = Integer.parseInt(req.getParameter("qty"));

        if (quantity > 5) {
            service.placeBulkOrder(userId, productId, quantity);
        } else {
            service.placeRegularOrder(userId, productId, quantity);
        }

        resp.getWriter().println("Order processed!");
    }
}
export interface Asset {
  id: string;
  label: string;
  category: string;
  icon: string;
  snippet: string;
  description: string;
}

export const ASSETS: Asset[] = [
  { id: "math_pi",       label: "Pi",          category: "symbols", icon: "π",   snippet: 'pi_symbol = MathTex(r"\\pi", font_size=72, color=ORANGE)\nself.play(Write(pi_symbol))\nself.wait(1)',                                                                                                                                           description: "Pi symbol" },
  { id: "math_sigma",    label: "Sigma",        category: "symbols", icon: "Σ",   snippet: 'sigma = MathTex(r"\\sum_{i=1}^{n} i = \\frac{n(n+1)}{2}", font_size=48)\nself.play(Write(sigma))\nself.wait(1)',                                                                                                                                 description: "Summation formula" },
  { id: "math_integral", label: "Integral",     category: "symbols", icon: "∫",   snippet: 'integral = MathTex(r"\\int_a^b f(x)\\,dx", font_size=60, color=BLUE)\nself.play(Write(integral))\nself.wait(1)',                                                                                                                                 description: "Integral notation" },
  { id: "math_limit",    label: "Limit",        category: "symbols", icon: "lim", snippet: 'limit = MathTex(r"\\lim_{x \\to \\infty} \\frac{1}{x} = 0", font_size=48)\nself.play(Write(limit))\nself.wait(1)',                                                                                                                              description: "Limit notation" },
  { id: "shape_circle",  label: "Circle",       category: "shapes",  icon: "○",   snippet: 'circle = Circle(radius=1.5, color=BLUE, fill_opacity=0.3)\nself.play(Create(circle))\nself.wait(1)',                                                                                                                                             description: "Animated circle" },
  { id: "shape_square",  label: "Square",       category: "shapes",  icon: "□",   snippet: 'square = Square(side_length=2.5, color=GREEN, fill_opacity=0.3)\nself.play(Create(square))\nself.wait(1)',                                                                                                                                       description: "Animated square" },
  { id: "shape_triangle",label: "Triangle",     category: "shapes",  icon: "△",   snippet: 'triangle = Triangle(color=RED, fill_opacity=0.3).scale(1.5)\nself.play(Create(triangle))\nself.wait(1)',                                                                                                                                         description: "Animated triangle" },
  { id: "shape_polygon", label: "Hexagon",      category: "shapes",  icon: "⬡",   snippet: 'hexagon = RegularPolygon(n=6, color=PURPLE, fill_opacity=0.3).scale(1.5)\nself.play(Create(hexagon))\nself.wait(1)',                                                                                                                             description: "Regular hexagon" },
  { id: "arrow_right",   label: "Arrow",        category: "arrows",  icon: "→",   snippet: 'arrow = Arrow(LEFT * 2, RIGHT * 2, buff=0, color=YELLOW)\nself.play(GrowArrow(arrow))\nself.wait(1)',                                                                                                                                            description: "Horizontal arrow" },
  { id: "arrow_double",  label: "Double Arrow", category: "arrows",  icon: "↔",   snippet: 'double_arrow = DoubleArrow(LEFT * 2, RIGHT * 2, buff=0, color=YELLOW)\nself.play(GrowArrow(double_arrow))\nself.wait(1)',                                                                                                                        description: "Double-headed arrow" },
  { id: "arrow_curved",  label: "Curved Arrow", category: "arrows",  icon: "↪",   snippet: 'curved = CurvedArrow(LEFT * 2 + DOWN, RIGHT * 2 + DOWN, color=ORANGE)\nself.play(Create(curved))\nself.wait(1)',                                                                                                                                 description: "Curved arrow" },
  { id: "grid_axes",     label: "Axes",         category: "grids",   icon: "⊹",   snippet: 'axes = Axes(\n    x_range=[-4, 4, 1], y_range=[-3, 3, 1],\n    axis_config={"color": GRAY, "include_numbers": True}\n)\nself.play(Create(axes))\nself.wait(1)',                                                                                  description: "Coordinate axes" },
  { id: "grid_plane",    label: "Grid",         category: "grids",   icon: "⊞",   snippet: 'plane = NumberPlane(\n    x_range=[-4, 4, 1], y_range=[-3, 3, 1],\n    background_line_style={"stroke_color": BLUE_D, "stroke_opacity": 0.5}\n)\nself.play(Create(plane))\nself.wait(1)',                                                       description: "Full coordinate grid" },
  { id: "grid_polar",    label: "Polar Grid",   category: "grids",   icon: "◎",   snippet: 'polar = PolarPlane(radius_max=3).add_coordinates()\nself.play(Create(polar))\nself.wait(1)',                                                                                                                                                     description: "Polar coordinate grid" },
];

export const ASSETS_BY_CATEGORY = ASSETS.reduce((acc, asset) => {
  acc[asset.category] = acc[asset.category] || [];
  acc[asset.category].push(asset);
  return acc;
}, {} as Record<string, Asset[]>);

package be.ghostwritertje.webapp.charts;

import be.ghostwritertje.services.NumberDisplay;
import be.ghostwritertje.views.budgetting.CategoryGroupView;
import be.ghostwritertje.views.budgetting.CategoryView;
import com.googlecode.wickedcharts.highcharts.options.ChartOptions;
import com.googlecode.wickedcharts.highcharts.options.DataLabels;
import com.googlecode.wickedcharts.highcharts.options.PixelOrPercent;
import com.googlecode.wickedcharts.highcharts.options.SeriesType;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;
import com.googlecode.wickedcharts.highcharts.options.color.HighchartsColor;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.highcharts.options.series.Series;
import org.apache.wicket.model.IModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Jorandeboever
 * Date: 29-Apr-17.
 */
public class PieChartBuilder extends ChartBuilderSupport<PieChartBuilder> {

    PieChartBuilder() {
        ChartOptions chartOptions = new ChartOptions();
        chartOptions
                .setType(SeriesType.PIE);
        this.getOptions().setChartOptions(chartOptions);
    }

    public PieChartBuilder setCategoryGroups(IModel<List<CategoryGroupView>> categoryGroups) {
        this.consume(options -> {
            List<Series<?>> seriesList = new ArrayList<>();
            DataLabels dataLabels1 = new DataLabels();
            dataLabels1.setColor(new HexColor("#ffffff"));
            dataLabels1.setDistance(-30);

            PointSeries pointSeries1 = new PointSeries();
            pointSeries1.setType(SeriesType.PIE);
            pointSeries1.setSize(new PixelOrPercent(60, PixelOrPercent.Unit.PERCENT));
            pointSeries1.setDataLabels(dataLabels1);

            PointSeries pointSeries2 = new PointSeries();
            pointSeries2.setType(SeriesType.PIE);
            pointSeries2.setInnerSize(new PixelOrPercent(60, PixelOrPercent.Unit.PERCENT));
            pointSeries2.setDataLabels(new DataLabels());

            List<CategoryGroupView> list = categoryGroups.getObject()
                    .stream()
                    .sorted(Comparator.comparing(CategoryGroupView::getNumberDisplayValue).reversed())
                    .collect(Collectors.toList());
            BigDecimal total = list.stream().map(CategoryGroupView::getNumberDisplayValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            Point rest = new Point("other", 0, new HighchartsColor(0));
            int i = 2;
            for (CategoryGroupView categoryGroup : list) {
                if (i < 10 && categoryGroup.getNumberDisplayValue().compareTo(total.divide(new BigDecimal("10"), RoundingMode.HALF_DOWN)) >= 0) {
                    HighchartsColor color = new HighchartsColor(i);
                    pointSeries1.addPoint(new Point(categoryGroup.getDisplayValue(), categoryGroup.getNumberDisplayValue(), color));

                    Float brightness = 0.01F;
                    for (CategoryView category : categoryGroup.getCategoryList()) {
                        pointSeries2.addPoint(new Point(category.getDisplayValue(), category.getNumberDisplayValue(), color.brighten(brightness)));
                        brightness += 0.05F;
                    }
                } else {
                    rest.setY(categoryGroup.getNumberDisplayValue().add(new BigDecimal(rest.getY().toString())));
                }

                i++;
            }
            pointSeries1.addPoint(rest);
            pointSeries2.addPoint(rest);
            seriesList.add(pointSeries1);
            seriesList.add(pointSeries2);

            options.setSeries(seriesList);
        });

        return this.self();
    }

    public PieChartBuilder addPoints(IModel<List<NumberDisplay>> numberDisplaysModel) {
        this.consume(options -> {
            List<Series<?>> seriesList = new ArrayList<>();
            DataLabels dataLabels1 = new DataLabels();
            dataLabels1.setColor(new HexColor("#ffffff"));

            PointSeries pointSeries1 = new PointSeries();
            pointSeries1.setType(SeriesType.PIE);
            pointSeries1.setDataLabels(dataLabels1);

            numberDisplaysModel.getObject().forEach(numberDisplay -> pointSeries1.addPoint(new Point(numberDisplay.getDisplayValue(), numberDisplay.getNumberDisplayValue())));
            seriesList.add(pointSeries1);

            options.setSeries(seriesList);
        });

        return this.self();
    }

}

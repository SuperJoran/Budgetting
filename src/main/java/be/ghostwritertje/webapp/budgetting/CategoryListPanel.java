package be.ghostwritertje.webapp.budgetting;

import be.ghostwritertje.domain.Person;
import be.ghostwritertje.domain.budgetting.CategoryGroup;
import be.ghostwritertje.domain.budgetting.CategoryType;
import be.ghostwritertje.services.NumberDisplay;
import be.ghostwritertje.services.budgetting.CategoryGroupService;
import be.ghostwritertje.services.budgetting.CategoryGroupViewService;
import be.ghostwritertje.services.budgetting.CategoryService;
import be.ghostwritertje.views.budgetting.CategoryGroupView;
import be.ghostwritertje.webapp.charts.ChartBuilderFactory;
import be.ghostwritertje.webapp.datatable.ColumnBuilderFactory;
import be.ghostwritertje.webapp.datatable.DataTableBuilderFactory;
import be.ghostwritertje.webapp.link.LinkBuilderFactory;
import be.ghostwritertje.webapp.model.DomainObjectListModel;
import be.ghostwritertje.webapp.model.LoadableListModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;

import java.util.List;

/**
 * Created by Jorandeboever on 5/6/2017.
 */
public class CategoryListPanel extends GenericPanel<Person> {

    private static final long serialVersionUID = -1312028287033905928L;
    @SpringBean
    private CategoryGroupService categoryGroupService;

    @SpringBean
    private CategoryService categoryService;

    private final IModel<List<CategoryGroup>> categoryGroupListModel;
    private final IModel<List<CategoryGroupView>> categoryGroupViewListModel;

    public CategoryListPanel(String id, IModel<Person> model) {
        super(id, model);
        this.categoryGroupListModel = new DomainObjectListModel<CategoryGroup, CategoryGroupService>(
                this.categoryGroupService,
                categoryGroupCategoryGroupService -> categoryGroupCategoryGroupService.findByAdministrator(this.getModelObject())
        );
        this.categoryGroupViewListModel = new ExpensesCategoryGroupViewListModel(model);

    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        LinkBuilderFactory.ajaxLink(newCategoryGroup())
                .usingDefaults()
                .body(new ResourceModel("new"))
                .attach(this, "new");

        DataTableBuilderFactory.<CategoryGroup, String>simple()
                .addColumn(ColumnBuilderFactory.<CategoryGroup, String>simple(CategoryGroup::getName).build(new ResourceModel("category")))
                .addColumn(ColumnBuilderFactory.actions(new ResourceModel("actions"), editCategoryGroup(), deleteCategoryGroup()))
                .attach(this, "dataTable", this.categoryGroupListModel);


        ChartBuilderFactory.pieChart()
                .title("Expenses")
//                .name("Statements")
                .subTitle("2016")
                .setCategoryGroups(this.categoryGroupViewListModel)
                .attach(this, "expenses");

        ChartBuilderFactory.pieChart()
                .title("Income")
                .subTitle("2016")
//                .name("Statements")
                .addPoints(new CountCategoryStatementsListModel(this.getModel()))
                .attach(this, "income");
    }

    private static SerializableBiConsumer<AjaxRequestTarget, AjaxLink<Object>> newCategoryGroup() {
        return (ajaxRequestTarget, components) -> {
            CategoryListPanel parent = components.findParent(CategoryListPanel.class);
            components.setResponsePage(new CategoryGroupPage(new Model<>(new CategoryGroup(parent.getModelObject()))));

        };
    }

    private static SerializableBiConsumer<AjaxRequestTarget, AjaxLink<CategoryGroup>> deleteCategoryGroup() {
        return (ajaxRequestTarget, components) -> {
            CategoryListPanel parent = components.findParent(CategoryListPanel.class);
            parent.categoryGroupService.delete(components.getModelObject());
            parent.categoryGroupListModel.setObject(null);
            ajaxRequestTarget.add(parent);
        };
    }

    private static SerializableBiConsumer<AjaxRequestTarget, AjaxLink<CategoryGroup>> editCategoryGroup() {
        return (ajaxRequestTarget, components) -> {
            components.setResponsePage(new CategoryGroupPage(components.getModel()));
        };
    }

    private static SerializableBiConsumer<AjaxRequestTarget, AjaxLink<Object>> assignCategories() {
        return (ajaxRequestTarget, components) -> {
            CategoryListPanel parent = components.findParent(CategoryListPanel.class);
            parent.categoryService.attemptToAssignCategoriesAutomaticallyForPerson(parent.getModelObject());
            ajaxRequestTarget.add(parent);
        };
    }

    private static final class CountCategoryStatementsListModel extends LoadableListModel<NumberDisplay> {

        @SpringBean
        private CategoryService categoryService;

        private final IModel<Person> administratorModel;

        private CountCategoryStatementsListModel(IModel<Person> administratorModel) {
            this.administratorModel = administratorModel;
        }

        @Override
        protected List<NumberDisplay> load() {
            return this.categoryService.findSumByAdministrator(this.administratorModel.getObject(), CategoryType.INCOME);
        }
    }

    private static final class ExpensesCategoryGroupViewListModel extends LoadableListModel<CategoryGroupView> {
        private static final long serialVersionUID = 534003398295017305L;

        @SpringBean
        private CategoryGroupViewService categoryGroupViewService;

        private final IModel<Person> administratorModel;

        private ExpensesCategoryGroupViewListModel(IModel<Person> administratorModel) {
            this.administratorModel = administratorModel;
        }

        @Override
        protected List<CategoryGroupView> load() {
            return this.categoryGroupViewService.findByAdministratorOrderByName(this.administratorModel.getObject(), CategoryType.EXPENSE);
        }
    }

}

package be.ghostwritertje.webapp.budgetting;

import be.ghostwritertje.domain.Person;
import be.ghostwritertje.domain.budgetting.BankAccount;
import be.ghostwritertje.services.budgetting.BankAccountService;
import be.ghostwritertje.services.budgetting.CategoryService;
import be.ghostwritertje.webapp.BasePage;
import be.ghostwritertje.webapp.datatable.ColumnBuilderFactory;
import be.ghostwritertje.webapp.datatable.DataTableBuilderFactory;
import be.ghostwritertje.webapp.link.LinkBuilderFactory;
import be.ghostwritertje.webapp.model.DomainObjectListModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;

/**
 * Created by Jorandeboever
 * Date: 15-Apr-17.
 */
public class BankAccountListPage extends BasePage<Person> {
    private static final long serialVersionUID = -3308755123541566574L;
    @SpringBean
    private BankAccountService bankAccountService;

    @SpringBean
    private CategoryService categoryService;

    public BankAccountListPage(IModel<Person> model) {
        super(model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        LinkBuilderFactory.ajaxLink(newBankAccount())
                .usingDefaults()
                .attach(this, "new", this.getModel());

        LinkBuilderFactory.pageLink(this::getModel, StatementListPage::new)
                .usingDefaults()
                .attach(this, "statements");

        this.add(DataTableBuilderFactory.<BankAccount, String>simple()
                .addColumn(ColumnBuilderFactory.custom(new ResourceModel(
                                "name"),
                        (s, bankAccountIModel) -> LinkBuilderFactory.pageLink(() -> bankAccountIModel,
                                im -> new BankAccountPage(bankAccountIModel))
                                .usingDefaults()
                                .body(LambdaModel.of(bankAccountIModel, BankAccount::getName))
                                .build(s)))
//                .addColumn(new LambdaColumn<>(new ResourceModel("bank"), bankAccount -> bankAccount.getBank().getName()))I02_BANKACCOUNT
                .addColumn(new LambdaColumn<>(new ResourceModel("username"),  b -> b.getAdministrator().getUsername()))
                .addColumn(new LambdaColumn<>(new ResourceModel("balance"), BankAccount::getBalance))
                .addColumn(ColumnBuilderFactory.actions(new ResourceModel("actions"), (target, link) -> this.setResponsePage(new BankAccountPage(link.getModel())),
                        (target, link) -> {
                            this.bankAccountService.delete(link.getModelObject());
                            link.setResponsePage(new BankAccountListPage(BankAccountListPage.this.getModel()));
                        }
                ))
                .build("bankAccounts", new DomainObjectListModel<BankAccount, BankAccountService>(this.bankAccountService,service ->  service.findByOwner(this.getModelObject()))));

        this.add(new BankAccountsGraph("graph", this.getModel()));

        this.add(new CategoryListPanel("categories", this.getModel()));
    }


    private static SerializableBiConsumer<AjaxRequestTarget, AjaxLink<Person>> newBankAccount() {
        return (target, components) -> {
            Person person = components.getModelObject();

            BankAccount bankAccount = new BankAccount();
            bankAccount.setOwner(person);
            bankAccount.setAdministrator(person);

            components.setResponsePage(new BankAccountPage(new Model<>(bankAccount)));
        };
    }
}

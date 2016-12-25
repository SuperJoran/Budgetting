package be.ghostwritertje.webapp.link;

import be.ghostwritertje.webapp.form.BaseForm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.lambda.WicketBiConsumer;
import org.apache.wicket.model.IModel;

/**
 * Created by Jorandeboever
 * Date: 24-Dec-16.
 */
public class AjaxSubmitLinkBuilder extends LinkBuilder<AjaxSubmitLinkBuilder, AjaxSubmitLink> {
    AjaxSubmitLinkBuilder() {
    }

    @Override
    AjaxSubmitLink buildLink(String id, WicketBiConsumer<AjaxRequestTarget, AjaxSubmitLink> submitConsumer) {
        return new MySubmitLink(id, submitConsumer);
    }

    private static class MySubmitLink extends AjaxSubmitLink {
        private final WicketBiConsumer<AjaxRequestTarget, AjaxSubmitLink> submitConsumer;

        private MySubmitLink(String id, WicketBiConsumer<AjaxRequestTarget, AjaxSubmitLink> submitConsumer) {
            super(id);
            this.submitConsumer = submitConsumer;
        }

        @Override
        protected void onSubmit(AjaxRequestTarget target) {
            super.onSubmit(target);
            BaseForm parent = this.findParent(BaseForm.class);
            IModel<BaseForm.FormMode> formModeModel = parent.getFormModeModel();
            formModeModel.setObject(BaseForm.FormMode.READ);
            this.submitConsumer.accept(target, this);
            target.add(parent);
        }
    }
}
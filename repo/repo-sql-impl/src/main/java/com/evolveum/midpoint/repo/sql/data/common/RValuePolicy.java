/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.repo.sql.data.common;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.data.common.embedded.RPolyString;
import com.evolveum.midpoint.repo.sql.util.DtoTranslationException;
import com.evolveum.midpoint.repo.sql.util.RUtil;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ValuePolicyType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Collection;

/**
 * @author lazyman
 */
@Entity
@ForeignKey(name = "fk_value_policy")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name_norm"}))
@org.hibernate.annotations.Table(appliesTo = "m_value_policy",
        indexes = {@Index(name = "iValuePolicy", columnNames = "name_orig")})
public class RValuePolicy extends RObject<ValuePolicyType> {

    private RPolyString name;

    @Embedded
    public RPolyString getName() {
        return name;
    }

    public void setName(RPolyString name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RValuePolicy that = (RValuePolicy) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public static void copyToJAXB(RValuePolicy repo, ValuePolicyType jaxb, PrismContext prismContext,
                                  Collection<SelectorOptions<GetOperationOptions>> options) throws
            DtoTranslationException {
        RObject.copyToJAXB(repo, jaxb, prismContext, options);

        jaxb.setName(RPolyString.copyToJAXB(repo.getName()));
    }

    public static void copyFromJAXB(ValuePolicyType jaxb, RValuePolicy repo, PrismContext prismContext) throws
            DtoTranslationException {
        RObject.copyFromJAXB(jaxb, repo, prismContext);

        repo.setName(RPolyString.copyFromJAXB(jaxb.getName()));
    }

    @Override
    public ValuePolicyType toJAXB(PrismContext prismContext, Collection<SelectorOptions<GetOperationOptions>> options)
            throws DtoTranslationException {
        ValuePolicyType policy = new ValuePolicyType();
        RUtil.revive(policy, prismContext);
        RValuePolicy.copyToJAXB(this, policy, prismContext, options);

        return policy;
    }
}

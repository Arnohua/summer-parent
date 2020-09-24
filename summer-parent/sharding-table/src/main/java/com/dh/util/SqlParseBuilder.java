package com.dh.util;

/**
 * @author dinghua
 * @date 2020/9/16
 * @since v1.0.0
 */
public class SqlParseBuilder {

    private static final char LINE_END = '\n';

    private static final char SPACE = ' ';

    private static final char PARENTHESIS = '(';

    private static final char POINT = '.';

    private static final char COMMA = ';';

    public static String parse(String sql,String target,String replacement){
        if (sql == null || target == null || replacement == null) {
            return sql;
        }
        int idx = sql.indexOf(target);
        if (idx == -1) {
            return sql;
        }
        int replacementLength = replacement.length();
        int targetLength = target.length();
        while (idx != -1){
            if(idx == 0){
                idx = sql.indexOf(target,idx + targetLength);
                continue;
            }
            char c1 = sql.charAt(idx - 1);
            char c2 = sql.charAt(idx + targetLength);
            if(isReplace(c1,c2)){
                sql = sql.substring(0,idx).concat(replacement).concat(sql.substring(idx + targetLength));
                idx = sql.indexOf(target,idx + replacementLength);
            } else {
                idx = sql.indexOf(target,idx + targetLength);
            }
        }
        return sql;
    }

    public static String parseV2(String sql,String target,String replacement){
        if (sql == null || target == null || replacement == null) {
            return sql;
        }
        int start = sql.indexOf(target);
        if (start == -1) {
            return sql;
        }
        char[] src = sql.toCharArray();
        int offset = 0;
        final StringBuilder builder = new StringBuilder();
        int targetLength = target.length();
        while(start > -1){
            if(start == 0){
                start = sql.indexOf(target,start + targetLength);
                if(start == -1){
                    return sql;
                }
                builder.append(src,0,start);
                continue;
            } else {
                builder.append(src,offset,(start - offset));
            }
            offset = start;
            char c1 = src[start - 1];
            char c2 = src[start + targetLength];
            if(isReplace(c1,c2)){
                builder.append(replacement);
                offset += targetLength;
            }
            start = sql.indexOf(target,start + targetLength);
        }
        int length = sql.length();
        if(offset < length){
            builder.append(src,offset, (length - offset));
        }
        return builder.toString();
    }

    private static boolean isReplace(char c1,char c2){
        return (LINE_END == c1 || SPACE == c1 || POINT == c1)
                && (LINE_END == c2 || SPACE == c2 ||  COMMA == c2 || PARENTHESIS == c2);
    }

    public static void main(String[] args) {
        String sql = "select\n" +
                "    \t    pd.id AS id,\n" +
                "            pd.tid AS tid,\n" +
                "            pdf.tid AS feeTid,\n" +
                "\t        pd.business_no AS businessNo,\n" +
                "\t        pd.supplier_service_code AS supplierServiceCode,\n" +
                "\t        pd.supplier_service_name AS supplierServiceName,\n" +
                "\t        pd.item_qty AS itemQty,\n" +
                "\t        pd.checkout_weight AS checkoutWeight,\n" +
                "\t        pd.billing_weight AS billingWeight,\n" +
                "\t        pd.amount_billing AS amountBilling,\n" +
                "            pdf.amount_diff AS amountDiff,\n" +
                "            (pdf.fee_amount + pdf.amount_diff) AS amountPayable,\n" +
                "            pdf.currency_code AS currencyCode,\n" +
                "\t        pd.departure AS departure,\n" +
                "\t        pd.destination_area AS destinationArea,\n" +
                "\t        pd.received_flag AS receivedFlag,\n" +
                "\t        pd.track_flag AS trackFlag,\n" +
                "\t        pd.fpx_account AS fpxAccount,\n" +
                "\t        pd.fpx_business AS fpxBusiness,\n" +
                "\t        pd.fpx_channel_mnemonic AS fpxChannelMnemonic,\n" +
                "\t        pd.supplier_no AS supplierNo,\n" +
                "\t        pd.cargo_type AS cargoType,\n" +
                "\t        pd.post_code AS postCode,\n" +
                "\t        pd.checkin_date AS checkinDate,\n" +
                "        \tpd.checkout_Date AS checkoutDate,\n" +
                "        \tpd.receipt_date AS receiptDate,\n" +
                "\t        pd.business_date AS businessDate,\n" +
                "            pdf.settled_flag AS settledFlag,\n" +
                "            pd.updated_by AS updatedBy,\n" +
                "            pd.billing_date AS billingDate,\n" +
                "\t        pd.remark,\n" +
                "\t        CASE pd.`status`\n" +
                "\t\t\t\tWHEN 'PR' THEN\n" +
                "\t\t\t\t\t'待收货'\n" +
                "\t\t\t\tWHEN 'PB' THEN\n" +
                "\t\t\t\t\t'待计费'\n" +
                "\t\t\t\tWHEN 'BB' THEN\n" +
                "\t\t\t\t\t'计费中'\n" +
                "\t\t\t\tWHEN 'BC' THEN\n" +
                "\t\t\t\t\t'已计费'\n" +
                "\t\t\t\tWHEN 'RB' THEN\n" +
                "\t\t\t\t\t'待重计费'\n" +
                "\t\t\t\tWHEN 'BF' THEN\n" +
                "\t\t\t\t\t'计费失败'\n" +
                "                WHEN 'I' THEN\n" +
                "                   \t '已作废'\n" +
                "\t\t\t\tELSE\n" +
                "\t\t\t\tpd.`status`\n" +
                "\t\t\tEND AS status,\n" +
                "            pd.customer_waybill_no AS customerWaybillNo,\n" +
                "            concat(pd.fpx_business, \"#\", if(pd.business_status = '', '-', pd.business_status)) AS businessStatus,\n" +
                "            pd.return_flag AS returnFlag,\n" +
                "            pd.salesman AS salesman,\n" +
                "            pd.sales_customer_service AS salesCustomerService,\n" +
                "            pd.customer_code AS customerCode,\n" +
                "            pd.customer_name AS customerName,\n" +
                "            pd.account_period_payable AS accountPeriodPayable,\n" +
                "            pdf.account_period_actual AS accountPeriodActual,\n" +
                "            pd.extra_amount AS extraAmount,\n" +
                "            pdf.detail_id AS detailId,\n" +
                "            pdf.fee_code AS feeCode,\n" +
                "\t        concat(pdf.fee_code,'DIFF') AS feeCodeDiff,\n" +
                "            pd.volume_weight AS volumeWeight,\n" +
                "            pd.declared_value AS declaredValue,\n" +
                "            pd.declared_currency AS declaredCurrency,\n" +
                "            pdf.fee_amount AS feeAmount\n" +
                "   from " +
                "   payable_detail pd\n" +
                "        LEFT JOIN payable_detail_fee pdf ON pdf.detail_id = pd.id ";

        /*sql = parseV2(sql,"payable_detail","payable_detail_other");
        //System.out.println(parseV2(sql,"payable_detail","payable_detail_other"));
        System.out.println(parseV2(sql,"payable_detail_fee","payable_detail_fee_other"));

        long start1 = System.currentTimeMillis();
        for(int i=0;i<1000000;i++){
            parse(sql,"payable_detail","payable_detail_other");
        }
        System.out.println(System.currentTimeMillis() - start1);

        long start2 = System.currentTimeMillis();
        for(int i=0;i<1000000;i++){
            parseV2(sql,"payable_detail","payable_detail_other");
        }
        System.out.println(System.currentTimeMillis() - start2);*/

        System.out.println(Math.pow(3,2));
    }


}

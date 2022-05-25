package mymatch.love.dynamicprofile;


/**
 * Created by lenovo on 2/23/2016.
 */
public interface ItemClickListener {
    void itemClicked(ViewProfileFieldsBean item);
    void itemClicked(ViewProfileSectionBean section);
    void viewContact(ViewProfileSectionBean section);
    void lastSectionExpand(ViewProfileSectionBean section);
}

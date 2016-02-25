package co.touchlab.researchstack.skin.ui.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.touchlab.researchstack.backbone.helpers.LogExt;
import co.touchlab.researchstack.backbone.utils.ResUtils;
import co.touchlab.researchstack.glue.R;
import co.touchlab.researchstack.skin.ResourceManager;
import co.touchlab.researchstack.skin.model.StudyOverviewModel;

public class StudyLandingLayout extends ScrollView
{

    private TextView  titleView;
    private TextView  subtitleView;
    private ImageView logoView;
    private Button    readConsent;
    private Button    emailConsent;

    public StudyLandingLayout(Context context)
    {
        super(context);
        init();
    }

    public StudyLandingLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public StudyLandingLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_study_landing, this, true);

        logoView = (ImageView) findViewById(R.id.layout_studyoverview_landing_logo);
        titleView = (TextView) findViewById(R.id.layout_studyoverview_landing_title);
        subtitleView = (TextView) findViewById(R.id.layout_studyoverview_landing_subtitle);
        readConsent = (Button) findViewById(R.id.layout_studyoverview_landing_read);
        emailConsent = (Button) findViewById(R.id.layout_studyoverview_landing_email);
    }

    public void setData(StudyOverviewModel.Question data)
    {
        logoView.setImageResource(ResourceManager.getInstance().getLargeLogoDiseaseIcon());

        titleView.setText(data.getTitle());
        if (! TextUtils.isEmpty(data.getDetails()))
        {
            subtitleView.setText(data.getDetails());
        }
        else
        {
            subtitleView.setVisibility(View.GONE);
        }

        if("yes".equals(data.getShowConsent()))
        {
            readConsent.setOnClickListener(v -> {

                //TODO Clean this up, should no write/read on main thread
                File consentFile = getConsentFormFileFromExternalStorage();

                //TODO This may fail and throw an ActivityNotFoundException for type "application/pdf"
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(consentFile), "application/pdf");
                v.getContext().startActivity(intent);
            });
        }
        else
        {
            readConsent.setVisibility(View.GONE);
        }

        emailConsent.setOnClickListener(v -> {

            //TODO Clean this up, should no write/read on main thread
            File consentFile = getConsentFormFileFromExternalStorage();
            String appName = ResUtils.getApplicationName(getContext());
            String emailSubject = getResources().getString(R.string.study_overview_email_subject,
                    appName);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(consentFile));

            String title = getContext().getString(R.string.send_email);
            getContext().startActivity(Intent.createChooser(intent, title));
        });
    }

    /**
     * TODO Fix crash when file has been deleted
     * TODO Check if SDCard is mounted
     *
     * @return Consent form pdf
     */
    @NonNull
    private File getConsentFormFileFromExternalStorage()
    {
        LogExt.d(getClass(), "getConsentFormFileFromExternalStorage() - - - - - - ");
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        String basepath = extStorageDirectory + "/" + ResUtils.getExternalSDAppFolder();

        int fileResId = ResourceManager.getInstance().getConsentPDF();
        String formName = getResources().getResourceEntryName(fileResId);
        String fileName = formName + ".pdf";

        File consentFile = new File(basepath, fileName);
        LogExt.d(getClass(), "File Path: " + consentFile.getAbsolutePath());

        if(! consentFile.exists())
        {
            LogExt.d(getClass(), "File does not exist");

            consentFile.getParentFile().mkdirs();
            LogExt.d(getClass(), "Created file directory on external storage");

            try
            {
                copy(getResources().openRawResource(fileResId), consentFile);
                LogExt.d(getClass(), "File copied to external storage");
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return consentFile;
    }

    private void copy(InputStream in, File dst) throws IOException
    {
        FileOutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;

        while((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }
}
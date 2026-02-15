use jni::objects::JClass;
use jni::sys::{jboolean, jint};
use jni::JNIEnv;
use log::info;
use once_cell::sync::OnceCell;
use std::collections::HashSet;
use std::fs;
use std::path::Path;
use ttf_parser::Face;
use quick_xml::events::Event;
use quick_xml::Reader;

static FONT_DETECTOR: OnceCell<FontDetector> = OnceCell::new();

struct FontDetector {
    system_unicode: HashSet<u32>,
}

impl FontDetector {
    fn new() -> Self {
        let _ = android_logger::init_once(
            android_logger::Config::default()
                .with_max_level(log::LevelFilter::Info)
        );

        info!("Initializing FontDetector");

        let effective_fonts = collect_effective_fonts();
        let system_unicode = scan_system_unicode(&effective_fonts);

        info!("FontDetector initialized with {} Unicode characters", system_unicode.len());

        FontDetector { system_unicode }
    }

    fn has_glyph(&self, codepoint: u32) -> bool {
        self.system_unicode.contains(&codepoint)
    }
}

fn collect_effective_fonts() -> HashSet<String> {
    let mut result = HashSet::new();

    let font_xml_dirs = [
        "/system/etc",
        "/system/product/etc",
        "/system/system_ext/etc",
        "/vendor/etc",
        "/product/etc",
    ];

    let font_xml_files = [
        "fonts.xml",
        "fonts_base.xml",
        "fonts_fallback.xml",
        "font_fallback.xml",
        "fonts_inter.xml",
        "fonts_slate.xml",
        "fonts_ule.xml",
        "fonts_flyme.xml",
        "flyme_fallback.xml",
        "flyme_font_fallback.xml",
    ];

    for dir in font_xml_dirs {
        let base = Path::new(dir);
        if !base.exists() {
            continue;
        }

        for file in font_xml_files {
            let path = base.join(file);
            if path.exists() {
                if let Ok(fonts) = parse_fonts_xml(&path) {
                    result.extend(fonts);
                }
            }
        }
    }

    result
}

fn parse_fonts_xml(path: &Path) -> Result<HashSet<String>, Box<dyn std::error::Error>> {
    let mut result = HashSet::new();

    let xml = fs::read_to_string(path)?;
    let mut reader = Reader::from_str(&xml);
    reader.config_mut().trim_text(true);

    let mut buf = Vec::new();
    let mut in_font = false;
    let mut ignore_font = false;
    let mut font_text = String::new();

    loop {
        match reader.read_event_into(&mut buf) {
            Ok(Event::Start(e)) if e.name().as_ref() == b"font" => {
                in_font = true;
                ignore_font = false;
                font_text.clear();
                for attr in e.attributes().flatten() {
                    if attr.key.as_ref() == b"fallbackFor" {
                        ignore_font = true;
                        break;
                    }
                }
            }
            Ok(Event::Text(e)) if in_font && !ignore_font => {
                let t = e.decode()?.trim().to_string();
                if !t.is_empty() && font_text.is_empty() {
                    font_text = t;
                }
            }
            Ok(Event::End(e)) if e.name().as_ref() == b"font" => {
                if in_font && !ignore_font {
                    if let Some(name) = normalize_font_filename(&font_text) {
                        result.insert(name);
                    }
                }
                in_font = false;
                ignore_font = false;
            }
            Ok(Event::Eof) => break,
            Err(_) => break,
            _ => {}
        }
        buf.clear();
    }

    Ok(result)
}

fn normalize_font_filename(s: &str) -> Option<String> {
    let s = s.trim();
    if s.ends_with(".ttf") || s.ends_with(".otf") || s.ends_with(".ttc") {
        Some(s.to_string())
    } else {
        None
    }
}

fn scan_system_unicode(effective_fonts: &HashSet<String>) -> HashSet<u32> {
    let mut result = HashSet::new();
    let font_dir = Path::new("/system/fonts");

    if !font_dir.exists() {
        log::warn!("Font directory not found: /system/fonts");
        return result;
    }

    if let Ok(entries) = fs::read_dir(font_dir) {
        for entry in entries.flatten() {
            let path = entry.path();
            if !path.is_file() {
                continue;
            }

            let ext = path.extension().and_then(|e| e.to_str());
            if !matches!(ext, Some("ttf") | Some("otf") | Some("ttc")) {
                continue;
            }

            let file_name = match path.file_name().and_then(|n| n.to_str()) {
                Some(n) => n,
                None => continue,
            };

            if !effective_fonts.is_empty() && !effective_fonts.contains(file_name) {
                continue;
            }

            if let Ok(data) = fs::read(&path) {
                if let Ok(face) = Face::parse(&data, 0) {
                    if let Some(cmap) = face.tables().cmap {
                        let mut local = HashSet::new();
                        for sub in cmap.subtables {
                            sub.codepoints(|cp| {
                                local.insert(cp);
                            });
                        }
                        if local.len() > 65535 {
                            log::warn!(
                                "Skipping font {}: cmap has {} mappings (> 65535)",
                                file_name,
                                local.len()
                            );
                            continue;
                        }
                        log::info!(
                            "Font {}: {} mappings",
                            file_name,
                            local.len()
                        );
                        result.extend(local);
                    }
                }
            }
        }
    }

    log::info!(
        "Total system unicode: {}",
        result.len()
    );
    result
}

#[no_mangle]
pub extern "system" fn Java_top_rainysummer_unihantest_FontDetector_nativeInit(
    _env: JNIEnv,
    _class: JClass,
) {
    let _ = FONT_DETECTOR.get_or_init(|| FontDetector::new());
}

#[no_mangle]
pub extern "system" fn Java_top_rainysummer_unihantest_FontDetector_nativeHasGlyph(
    _env: JNIEnv,
    _class: JClass,
    codepoint: jint,
) -> jboolean {
    if let Some(detector) = FONT_DETECTOR.get() {
        detector.has_glyph(codepoint as u32) as jboolean
    } else {
        false as jboolean
    }
}
